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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
        List<BorrowRequest> borrowRequests = borrowRequestRepository.findByBorrowerUsernameAndStatus(username, "BORROWED");
        return borrowRequests.stream().map(BorrowedBookDTO::new).collect(Collectors.toList());
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

    public AddBookResponse addBookToUser(String googleBookId, BookStatus status, User user) {
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
        return new AddBookResponse(userBookRepository.save(userBook));
    }

    private GoogleBookDTO fetchBookFromGoogleApi(String googleBookId) {
        String url = "https://www.googleapis.com/books/v1/volumes/" + googleBookId;

        GoogleBookDTO googleBookDTO = restTemplate.getForObject(url, GoogleBookDTO.class);
        if (googleBookDTO == null) {
            throw new NotFoundException("Book not found in Google Books API");
        }
        return googleBookDTO;
    }
}