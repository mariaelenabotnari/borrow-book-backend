package org.borrowbook.borrowbookbackend.service;

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

    public List<CollectionBooks> fetchBooksUser(String username) {
        List<UserBook> userBooks = userBookRepository.findByOwner_Username(username);
        List<CollectionBooks> collectionBooksList = new ArrayList<>();

        for (UserBook userBook: userBooks) {
            Book book = userBook.getBook();

            CollectionBooks collectionBook = new CollectionBooks();
            collectionBook.setTitle(book.getTitle());
            collectionBook.setAuthors(book.getAuthor());
            collectionBook.setImageLink(book.getImageLink());
            collectionBook.setStatus(userBook.getStatus().toString());

            collectionBooksList.add(collectionBook);
        }
        return collectionBooksList;
    }

    public List<BorrowedBooks> fetchBorrowedBooks(String username) {
        List<BorrowRequest> borrowRequests = borrowRequestRepository.findByBorrowerUsernameAndStatus(username, "borrowed");
        List<BorrowedBooks> borrowedBooksList = new ArrayList<>();

        for (BorrowRequest borrowRequest: borrowRequests) {
            BorrowedBooks borrowedBook = new BorrowedBooks();

            Book book = borrowRequest.getUserBook().getBook();
            borrowedBook.setTitle(book.getTitle());
            borrowedBook.setAuthors(book.getAuthor());
            borrowedBook.setImageLink(book.getImageLink());
            borrowedBook.setOwnerUsername(borrowRequest.getBorrower().getUsername());

            borrowedBooksList.add(borrowedBook);
        }
        return borrowedBooksList;
    }

    public void deleteBook(String username, Integer bookId) {
        UserBook bookToDelete = userBookRepository.findByOwner_UsernameAndBook_Id(username, bookId);

        if (bookToDelete == null) {
            throw new NotFoundException("Book not found");
        }
        userBookRepository.delete(bookToDelete);
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
                .collect(Collectors.toList());
    }

    public AddBookResponse addBookToUser(String googleBookId, BookStatus status, User user) {
        Optional<Book> existingBook = bookRepository.findByGoogleBookId(googleBookId);
        Book book;

        if (existingBook.isPresent()) {
            book = existingBook.get();
        } else {
            // Fetch book details from Google Books API
            GoogleBookDTO googleBookDTO = fetchBookFromGoogleApi(googleBookId);
            book = mapGoogleBookToBook(googleBookDTO);
            book = bookRepository.save(book);
        }

        // Get the managed User entity from database using email
        User owner = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Create UserBook entry
        UserBook userBook = new UserBook();
        userBook.setBook(book);
        userBook.setStatus(status);
        userBook.setOwner(owner);

        UserBook savedUserBook = userBookRepository.save(userBook);
        return new AddBookResponse(savedUserBook);
    }

    private GoogleBookDTO fetchBookFromGoogleApi(String googleBookId) {
        String url = "https://www.googleapis.com/books/v1/volumes/" + googleBookId;

        GoogleBookDTO googleBookDTO = restTemplate.getForObject(url, GoogleBookDTO.class);
        if (googleBookDTO == null) {
            throw new NotFoundException("Book not found in Google Books API");
        }
        return googleBookDTO;
    }

    private Book mapGoogleBookToBook(GoogleBookDTO googleBookDTO) {
        Book book = new Book();
        book.setGoogleBookId(googleBookDTO.getId());

        GoogleBookDTO.VolumeInfo volumeInfo = googleBookDTO.getVolumeInfo();
        if (volumeInfo != null) {
            book.setTitle(volumeInfo.getTitle());
            book.setAuthor(volumeInfo.getAuthors());
            book.setPublisher(volumeInfo.getPublisher());

            if (volumeInfo.getImageLinks() != null) {
                String imageLink = volumeInfo.getImageLinks().getThumbnail();
                if (imageLink == null) {
                    imageLink = volumeInfo.getImageLinks().getSmallThumbnail();
                }
                book.setImageLink(imageLink);
            }
        }

        return book;
    }

}
