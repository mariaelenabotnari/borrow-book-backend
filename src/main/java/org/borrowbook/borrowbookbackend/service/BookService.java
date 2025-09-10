package org.borrowbook.borrowbookbackend.service;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.entity.Book;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.borrowbook.borrowbookbackend.repository.BookRepository;
import org.borrowbook.borrowbookbackend.repository.BorrowRequestRepository;
import org.borrowbook.borrowbookbackend.repository.UserBookRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BookService {
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final BorrowRequestRepository borrowRequestRepository;

    public List<Book> fetchBooksUser(Integer userId) {
        List<UserBook> userBooks = userBookRepository.findByOwnerId(userId);
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
}
