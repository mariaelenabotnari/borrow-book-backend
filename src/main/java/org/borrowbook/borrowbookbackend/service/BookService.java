package org.borrowbook.borrowbookbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.exception.NotFoundException;
import org.borrowbook.borrowbookbackend.model.dto.*;
import org.borrowbook.borrowbookbackend.model.entity.Book;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;
import org.borrowbook.borrowbookbackend.repository.BookRepository;
import org.borrowbook.borrowbookbackend.repository.BorrowRequestRepository;
import org.borrowbook.borrowbookbackend.repository.UserBookRepository;
import org.borrowbook.borrowbookbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BookService {

    private final RestTemplate restTemplate;
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final UserRepository userRepository;

    @Value("${google.book.api.key}")
    private String GOOGLE_BOOK_API_KEY;

    public List<CollectionBookDTO> fetchBooksUser(String username) {
        List<UserBook> userBooks = userBookRepository.findByOwner_Username(username);
        return userBooks.stream().map(CollectionBookDTO::new).collect(Collectors.toList());
    }

    public List<BorrowedBookDTO> fetchBorrowedBooks(String username) {
        List<BorrowRequest> borrowRequests = borrowRequestRepository.findByBorrowerUsernameAndUserBookStatus(username, BookStatus.BORROWED);
        return borrowRequests.stream().map(BorrowedBookDTO::new).collect(Collectors.toList());
    }

    public List<UserBooksDTO> fetchUserBooks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        List<UserBooksDTO> booksList = new ArrayList<>();
        List<UserBook> userBooks = userBookRepository.findByOwner_Username(username);

        for (UserBook userBook : userBooks) {
            if (userBook.getStatus() == BookStatus.AVAILABLE) {
                Book book = userBook.getBook();
                UserBooksDTO userBooksDTO = new UserBooksDTO();
                userBooksDTO.setUserBookId(userBook.getId());
                userBooksDTO.setTitle(book.getTitle());
                userBooksDTO.setAuthors(book.getAuthor());
                userBooksDTO.setImageLink(book.getImageLink());
                userBooksDTO.setStatus(BookStatus.AVAILABLE);
                booksList.add(userBooksDTO);
            }
        }
        return booksList;
    }

    public void deleteBook(String username, int userBookId) {
        UserBook userBook = userBookRepository.findByIdAndOwner_Username(userBookId, username)
                .orElseThrow(() -> new EntityNotFoundException("Book not found or not owned by user"));
        userBookRepository.delete(userBook);
    }

    public List<BookSearchDTO> fetchBooksWithGoogle(String query) {
        String url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/books/v1/volumes")
                .queryParam("q", query)
                .queryParam("printType", "books")
                .queryParam("key", GOOGLE_BOOK_API_KEY)
                .build()
                .toUriString();
        GoogleBookResponeDTO response = restTemplate.getForObject(url, GoogleBookResponeDTO.class);

        return Optional.ofNullable(response)
                .map(GoogleBookResponeDTO::getItems)
                .orElseGet(List::of)
                .stream()
                .map(BookSearchDTO::new)
                .filter(bookSearchDTO -> bookSearchDTO.getTitle() != null)
                .collect(Collectors.toList());
    }

    public AddBookResponseDTO addBookToUser(String googleBookId, BookStatus status, User user) {
        Optional<Book> existingBook = bookRepository.findByGoogleBookId(googleBookId);
        Book book;

        if (existingBook.isPresent()) {
            book = existingBook.get();
        } else {
            GoogleBookDTO googleBookDTO = fetchBookFromGoogleApi(googleBookId);
            book = new Book(googleBookDTO);
            book = bookRepository.save(book);
        }
        UserBook userBook = new UserBook(status, user, book);
        return new AddBookResponseDTO(userBookRepository.save(userBook));
    }

    public PaginatedResultDTO<CollectionBookDTO> searchBooksByTitle(String title, PaginatedRequestDTO request) {
        Pageable pageable = PageRequest.of(
                request.getPageIndex() - 1,
                request.getPageSize(),
                Sort.by("book.title").ascending()
        );

        Page<UserBook> userBooksPage = userBookRepository
                .findByBookTitleContainingIgnoreCaseAndStatus(title, BookStatus.AVAILABLE, pageable);

        List<CollectionBookDTO> items = userBooksPage.getContent()
                .stream()
                .map(CollectionBookDTO::new)
                .collect(Collectors.toList());

        return new PaginatedResultDTO<>(
                request.getPageIndex(),
                request.getPageSize(),
                userBooksPage.getTotalElements(),
                items
        );
    }

    private GoogleBookDTO fetchBookFromGoogleApi(String googleBookId) {
        String url = "https://www.googleapis.com/books/v1/volumes/" + googleBookId;

        try {
            GoogleBookDTO googleBookDTO = restTemplate.getForObject(url, GoogleBookDTO.class);
            if (googleBookDTO == null) {
                throw new NotFoundException("Book not found in Google Books API");
            }
            return googleBookDTO;
        } catch (HttpServerErrorException e) {
            throw new NotFoundException("Google Books API is temporarily unavailable. Please try again later.");
        }
    }
}