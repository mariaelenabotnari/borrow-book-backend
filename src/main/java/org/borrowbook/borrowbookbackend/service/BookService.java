package org.borrowbook.borrowbookbackend.service;

import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.borrowbook.borrowbookbackend.repository.UserBookRepository;

import java.util.List;

public class BookService {
    UserBookRepository userBookRepository;

    public List<UserBook> fetchBooksUser(String username) {
        List<UserBook> userBooks = userBookRepository.findByOwner_Username(username);

        return userBooks;
    }
//
//    public List<UserBook> fetchBooksToBorrow(String username) {
//    }
}
