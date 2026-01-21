package org.borrowbook.borrowbookbackend.service;

import jakarta.persistence.EntityNotFoundException;
import org.borrowbook.borrowbookbackend.Role;
import org.borrowbook.borrowbookbackend.model.dto.*;
import org.borrowbook.borrowbookbackend.model.entity.Book;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;
import org.borrowbook.borrowbookbackend.repository.BookRepository;
import org.borrowbook.borrowbookbackend.repository.BorrowRequestRepository;
import org.borrowbook.borrowbookbackend.repository.UserBookRepository;
import org.borrowbook.borrowbookbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookService bookService;

    private User testUser;
    private User otherUser;
    private Book testBook;
    private UserBook testUserBook;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .build();

        otherUser = User.builder()
                .id(2)
                .username("otheruser")
                .email("other@example.com")
                .role(Role.USER)
                .build();

        testBook = new Book();
        testBook.setId(1);
        testBook.setGoogleBookId("googleBookId123");
        testBook.setTitle("Test Book Title");
        testBook.setAuthor(Arrays.asList("Author One", "Author Two"));
        testBook.setPublisher("Test Publisher");
        testBook.setImageLink("http://example.com/image.jpg");

        testUserBook = new UserBook();
        testUserBook.setId(1);
        testUserBook.setBook(testBook);
        testUserBook.setOwner(testUser);
        testUserBook.setStatus(BookStatus.AVAILABLE);
    }

    @Nested
    @DisplayName("fetchBooksUser tests")
    class FetchBooksUserTests {

        @Test
        @DisplayName("Should return list of user's books")
        void fetchBooksUser_WhenUserHasBooks_ShouldReturnBookList() {
            // Arrange
            UserBook userBook1 = createUserBook(1, testUser, testBook, BookStatus.AVAILABLE);
            Book book2 = createBook(2, "Second Book", "google456");
            UserBook userBook2 = createUserBook(2, testUser, book2, BookStatus.BORROWED);
            
            when(userBookRepository.findByOwner_Username(testUser.getUsername()))
                    .thenReturn(Arrays.asList(userBook1, userBook2));

            // Act
            List<CollectionBookDTO> result = bookService.fetchBooksUser(testUser.getUsername());

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(userBookRepository).findByOwner_Username(testUser.getUsername());
        }

        @Test
        @DisplayName("Should return empty list when user has no books")
        void fetchBooksUser_WhenUserHasNoBooks_ShouldReturnEmptyList() {
            // Arrange
            when(userBookRepository.findByOwner_Username(testUser.getUsername()))
                    .thenReturn(Collections.emptyList());

            // Act
            List<CollectionBookDTO> result = bookService.fetchBooksUser(testUser.getUsername());

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("fetchBorrowedBooks tests")
    class FetchBorrowedBooksTests {

        @Test
        @DisplayName("Should return list of borrowed books")
        void fetchBorrowedBooks_WhenUserHasBorrowedBooks_ShouldReturnList() {
            // Arrange
            BorrowRequest borrowRequest = createBorrowRequest(1, testUserBook, testUser, BookRequestStatus.ACCEPTED);
            
            when(borrowRequestRepository.findByBorrowerUsernameAndStatus(
                    testUser.getUsername(), BookRequestStatus.ACCEPTED))
                    .thenReturn(List.of(borrowRequest));

            // Act
            List<BorrowedBookDTO> result = bookService.fetchBorrowedBooks(testUser.getUsername());

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return empty list when user has no borrowed books")
        void fetchBorrowedBooks_WhenNoBorrowedBooks_ShouldReturnEmptyList() {
            // Arrange
            when(borrowRequestRepository.findByBorrowerUsernameAndStatus(
                    testUser.getUsername(), BookRequestStatus.ACCEPTED))
                    .thenReturn(Collections.emptyList());

            // Act
            List<BorrowedBookDTO> result = bookService.fetchBorrowedBooks(testUser.getUsername());

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("fetchUserBooks tests")
    class FetchUserBooksTests {

        @Test
        @DisplayName("Should return available books from target user")
        void fetchUserBooks_WhenTargetUserHasAvailableBooks_ShouldReturnList() {
            // Arrange
            when(userRepository.findByUsername(otherUser.getUsername()))
                    .thenReturn(Optional.of(otherUser));
            
            testUserBook.setOwner(otherUser);
            when(userBookRepository.findByOwner_Username(otherUser.getUsername()))
                    .thenReturn(List.of(testUserBook));
            
            when(borrowRequestRepository.findByBorrowerUsernameAndUserBookIdAndStatus(
                    testUser.getUsername(), testUserBook.getId(), BookRequestStatus.PENDING))
                    .thenReturn(Optional.empty());

            // Act
            List<UserBooksDTO> result = bookService.fetchUserBooks(
                    testUser.getUsername(), otherUser.getUsername());

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should exclude borrowed books from result")
        void fetchUserBooks_WhenBookIsBorrowed_ShouldExcludeFromResult() {
            // Arrange
            when(userRepository.findByUsername(otherUser.getUsername()))
                    .thenReturn(Optional.of(otherUser));
            
            testUserBook.setOwner(otherUser);
            testUserBook.setStatus(BookStatus.BORROWED);
            when(userBookRepository.findByOwner_Username(otherUser.getUsername()))
                    .thenReturn(List.of(testUserBook));

            // Act
            List<UserBooksDTO> result = bookService.fetchUserBooks(
                    testUser.getUsername(), otherUser.getUsername());

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when target user not found")
        void fetchUserBooks_WhenTargetUserNotFound_ShouldThrowException() {
            // Arrange
            when(userRepository.findByUsername("nonexistent"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> bookService.fetchUserBooks(testUser.getUsername(), "nonexistent"));
        }

        @Test
        @DisplayName("Should mark book as pending when request exists")
        void fetchUserBooks_WhenPendingRequestExists_ShouldMarkAsPending() {
            // Arrange
            when(userRepository.findByUsername(otherUser.getUsername()))
                    .thenReturn(Optional.of(otherUser));
            
            testUserBook.setOwner(otherUser);
            when(userBookRepository.findByOwner_Username(otherUser.getUsername()))
                    .thenReturn(List.of(testUserBook));
            
            BorrowRequest pendingRequest = createBorrowRequest(1, testUserBook, testUser, BookRequestStatus.PENDING);
            when(borrowRequestRepository.findByBorrowerUsernameAndUserBookIdAndStatus(
                    testUser.getUsername(), testUserBook.getId(), BookRequestStatus.PENDING))
                    .thenReturn(Optional.of(pendingRequest));

            // Act
            List<UserBooksDTO> result = bookService.fetchUserBooks(
                    testUser.getUsername(), otherUser.getUsername());

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).isPending());
        }
    }

    @Nested
    @DisplayName("deleteBook tests")
    class DeleteBookTests {

        @Test
        @DisplayName("Should delete book when user owns it")
        void deleteBook_WhenUserOwnsBook_ShouldDelete() {
            // Arrange
            when(userBookRepository.findByIdAndOwner_Username(testUserBook.getId(), testUser.getUsername()))
                    .thenReturn(Optional.of(testUserBook));

            // Act
            bookService.deleteBook(testUser.getUsername(), testUserBook.getId());

            // Assert
            verify(userBookRepository).delete(testUserBook);
        }

        @Test
        @DisplayName("Should throw exception when book not found or not owned")
        void deleteBook_WhenBookNotFoundOrNotOwned_ShouldThrowException() {
            // Arrange
            when(userBookRepository.findByIdAndOwner_Username(999, testUser.getUsername()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> bookService.deleteBook(testUser.getUsername(), 999));
            verify(userBookRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("addBookToUser tests")
    class AddBookToUserTests {

        @Test
        @DisplayName("Should use existing book when already in database")
        void addBookToUser_WhenBookExists_ShouldUseExisting() {
            // Arrange
            String googleBookId = "existingGoogleId";
            when(bookRepository.findByGoogleBookId(googleBookId))
                    .thenReturn(Optional.of(testBook));
            
            UserBook savedUserBook = new UserBook(BookStatus.AVAILABLE, testUser, testBook);
            savedUserBook.setId(1);
            when(userBookRepository.save(any(UserBook.class))).thenReturn(savedUserBook);

            // Act
            AddBookResponseDTO result = bookService.addBookToUser(googleBookId, BookStatus.AVAILABLE, testUser);

            // Assert
            assertNotNull(result);
            verify(bookRepository).findByGoogleBookId(googleBookId);
            verify(bookRepository, never()).save(any(Book.class)); // Should not save new book
            verify(userBookRepository).save(any(UserBook.class));
        }
    }

    // Helper methods to create test entities
    private UserBook createUserBook(int id, User owner, Book book, BookStatus status) {
        UserBook userBook = new UserBook();
        userBook.setId(id);
        userBook.setOwner(owner);
        userBook.setBook(book);
        userBook.setStatus(status);
        return userBook;
    }

    private Book createBook(int id, String title, String googleBookId) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setGoogleBookId(googleBookId);
        book.setAuthor(List.of("Test Author"));
        return book;
    }

    private BorrowRequest createBorrowRequest(int id, UserBook userBook, User borrower, BookRequestStatus status) {
        BorrowRequest borrowRequest = new BorrowRequest();
        borrowRequest.setId(id);
        borrowRequest.setUserBook(userBook);
        borrowRequest.setBorrower(borrower);
        borrowRequest.setStatus(status);
        borrowRequest.setCreatedAt(LocalDateTime.now());
        borrowRequest.setMeetingTime(LocalDateTime.now().plusDays(1));
        borrowRequest.setDueDate(LocalDate.now().plusWeeks(2));
        borrowRequest.setLocation("Library");
        return borrowRequest;
    }
}
