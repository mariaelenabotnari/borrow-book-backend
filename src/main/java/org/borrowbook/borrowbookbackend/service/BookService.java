package org.borrowbook.borrowbookbackend.service;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.BookSearchDTO;
import org.borrowbook.borrowbookbackend.model.dto.GoogleBookResponeDTO;
import org.borrowbook.borrowbookbackend.model.entity.Book;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.borrowbook.borrowbookbackend.repository.BookRepository;
import org.borrowbook.borrowbookbackend.repository.BorrowRequestRepository;
import org.borrowbook.borrowbookbackend.repository.UserBookRepository;
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

    @Value("${google.book.api.key}")
    private String GOOGLE_BOOK_API_KEY;

    public List<Book> fetchBooksUser(Integer userId) {
        List<UserBook> userBooks = userBookRepository.findByOwner_Id(userId);
        List<Book> books = new ArrayList<>();

        for (UserBook userBook: userBooks) {
            books.add(userBook.getBook());
        }
        return books;
    }

    public List<Book> fetchBorrowedBooks(Integer userId) {
        List<BorrowRequest> borrowRequests = borrowRequestRepository.findByBorrowerIdAndStatus(userId, "available");
        List<Book> borrowedBooks = new ArrayList<>();

        for (BorrowRequest borrowRequest: borrowRequests) {
            Book book = borrowRequest.getUserBook().getBook();
            borrowedBooks.add(book);
        }
        return borrowedBooks;
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
}
