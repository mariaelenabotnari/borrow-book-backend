package org.borrowbook.borrowbookbackend.service;

import jakarta.persistence.EntityNotFoundException;
import org.borrowbook.borrowbookbackend.Role;
import org.borrowbook.borrowbookbackend.exception.BookIsAlreadyBorrowedException;
import org.borrowbook.borrowbookbackend.exception.CantBorrowYourOwnBookException;
import org.borrowbook.borrowbookbackend.exception.PendingBorrowRequestExistsException;
import org.borrowbook.borrowbookbackend.model.dto.BorrowRequestDTO;
import org.borrowbook.borrowbookbackend.model.dto.BorrowRequestResponseDTO;
import org.borrowbook.borrowbookbackend.model.dto.PaginatedResultDTO;
import org.borrowbook.borrowbookbackend.model.entity.Book;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;
import org.borrowbook.borrowbookbackend.repository.BorrowRequestRepository;
import org.borrowbook.borrowbookbackend.repository.UserBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @InjectMocks
    private BorrowService borrowService;

    private User borrower;
    private User owner;
    private Book book;
    private UserBook userBook;
    private BorrowRequestDTO borrowRequestDTO;

    @BeforeEach
    void setUp() {
        // Common test fixtures
        borrower = User.builder()
                .id(1)
                .username("borrower")
                .email("borrower@test.com")
                .role(Role.USER)
                .build();

        owner = User.builder()
                .id(2)
                .username("owner")
                .email("owner@test.com")
                .role(Role.USER)
                .build();

        book = new Book();
        book.setId(1);
        book.setTitle("Test Book");
        book.setGoogleBookId("google123");

        userBook = new UserBook();
        userBook.setId(1);
        userBook.setBook(book);
        userBook.setOwner(owner);
        userBook.setStatus(BookStatus.AVAILABLE);

        borrowRequestDTO = new BorrowRequestDTO();
        borrowRequestDTO.setMeetingTime(LocalDateTime.now().plusDays(1));
        borrowRequestDTO.setDueDate(LocalDate.now().plusWeeks(2));
        borrowRequestDTO.setLocation("Library");
    }

    @Nested
    @DisplayName("saveBorrowRequest tests")
    class SaveBorrowRequestTests {

        @Test
        @DisplayName("Should save borrow request when all conditions are valid")
        void saveBorrowRequest_WhenAllConditionsValid_ShouldSaveRequest() {
            // Arrange
            Integer userBookId = 1;
            when(borrowRequestRepository.findByBorrowerUsernameAndUserBookIdAndStatus(
                    borrower.getUsername(), userBookId, BookRequestStatus.PENDING))
                    .thenReturn(Optional.empty());
            when(userBookRepository.findById(Long.valueOf(userBookId)))
                    .thenReturn(Optional.of(userBook));

            // Act
            borrowService.saveBorrowRequest(borrower, borrowRequestDTO, userBookId);

            // Assert
            ArgumentCaptor<BorrowRequest> captor = ArgumentCaptor.forClass(BorrowRequest.class);
            verify(borrowRequestRepository).save(captor.capture());
            
            BorrowRequest savedRequest = captor.getValue();
            assertEquals(borrower, savedRequest.getBorrower());
            assertEquals(userBook, savedRequest.getUserBook());
            assertEquals(BookRequestStatus.PENDING, savedRequest.getStatus());
            assertEquals(borrowRequestDTO.getLocation(), savedRequest.getLocation());
        }

        @Test
        @DisplayName("Should throw exception when pending request already exists")
        void saveBorrowRequest_WhenPendingRequestExists_ShouldThrowException() {
            // Arrange
            Integer userBookId = 1;
            BorrowRequest existingRequest = new BorrowRequest();
            when(borrowRequestRepository.findByBorrowerUsernameAndUserBookIdAndStatus(
                    borrower.getUsername(), userBookId, BookRequestStatus.PENDING))
                    .thenReturn(Optional.of(existingRequest));

            // Act & Assert
            PendingBorrowRequestExistsException exception = assertThrows(
                    PendingBorrowRequestExistsException.class,
                    () -> borrowService.saveBorrowRequest(borrower, borrowRequestDTO, userBookId)
            );
            
            assertEquals("You already have a pending borrow request for this book.", exception.getMessage());
            verify(borrowRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when book is not found")
        void saveBorrowRequest_WhenBookNotFound_ShouldThrowException() {
            // Arrange
            Integer userBookId = 999;
            when(borrowRequestRepository.findByBorrowerUsernameAndUserBookIdAndStatus(
                    borrower.getUsername(), userBookId, BookRequestStatus.PENDING))
                    .thenReturn(Optional.empty());
            when(userBookRepository.findById(Long.valueOf(userBookId)))
                    .thenReturn(Optional.empty());

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> borrowService.saveBorrowRequest(borrower, borrowRequestDTO, userBookId)
            );
            
            assertTrue(exception.getMessage().contains("UserBook not found"));
            verify(borrowRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when book is already borrowed")
        void saveBorrowRequest_WhenBookAlreadyBorrowed_ShouldThrowException() {
            // Arrange
            Integer userBookId = 1;
            userBook.setStatus(BookStatus.BORROWED);
            
            when(borrowRequestRepository.findByBorrowerUsernameAndUserBookIdAndStatus(
                    borrower.getUsername(), userBookId, BookRequestStatus.PENDING))
                    .thenReturn(Optional.empty());
            when(userBookRepository.findById(Long.valueOf(userBookId)))
                    .thenReturn(Optional.of(userBook));

            // Act & Assert
            BookIsAlreadyBorrowedException exception = assertThrows(
                    BookIsAlreadyBorrowedException.class,
                    () -> borrowService.saveBorrowRequest(borrower, borrowRequestDTO, userBookId)
            );
            
            assertEquals("This book is already borrowed by another user.", exception.getMessage());
            verify(borrowRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user tries to borrow own book")
        void saveBorrowRequest_WhenBorrowingOwnBook_ShouldThrowException() {
            // Arrange
            Integer userBookId = 1;
            userBook.setOwner(borrower); // Set owner to be the same as borrower
            
            when(borrowRequestRepository.findByBorrowerUsernameAndUserBookIdAndStatus(
                    borrower.getUsername(), userBookId, BookRequestStatus.PENDING))
                    .thenReturn(Optional.empty());
            when(userBookRepository.findById(Long.valueOf(userBookId)))
                    .thenReturn(Optional.of(userBook));

            // Act & Assert
            CantBorrowYourOwnBookException exception = assertThrows(
                    CantBorrowYourOwnBookException.class,
                    () -> borrowService.saveBorrowRequest(borrower, borrowRequestDTO, userBookId)
            );
            
            assertEquals("You cannot borrow your own book.", exception.getMessage());
            verify(borrowRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("acceptBorrowRequest tests")
    class AcceptBorrowRequestTests {

        @Test
        @DisplayName("Should accept borrow request and update book status")
        void acceptBorrowRequest_WhenValid_ShouldAcceptAndUpdateBookStatus() {
            // Arrange
            Integer borrowRequestId = 1;
            BorrowRequest borrowRequest = new BorrowRequest();
            borrowRequest.setId(borrowRequestId);
            borrowRequest.setUserBook(userBook);
            borrowRequest.setStatus(BookRequestStatus.PENDING);
            
            when(borrowRequestRepository.findByIdAndUserBookOwnerUsername(borrowRequestId, owner.getUsername()))
                    .thenReturn(Optional.of(borrowRequest));
            when(borrowRequestRepository.findByUserBookIdAndStatusAndIdNot(
                    userBook.getId(), BookRequestStatus.PENDING, borrowRequestId))
                    .thenReturn(Collections.emptyList());

            // Act
            borrowService.acceptBorrowRequest(owner, borrowRequestId);

            // Assert
            assertEquals(BookRequestStatus.ACCEPTED, borrowRequest.getStatus());
            assertEquals(BookStatus.BORROWED, userBook.getStatus());
            assertNotNull(borrowRequest.getBorrowedAt());
            verify(userBookRepository).save(userBook);
            verify(borrowRequestRepository).save(borrowRequest);
        }

        @Test
        @DisplayName("Should reject other pending requests when accepting one")
        void acceptBorrowRequest_WhenOtherPendingRequestsExist_ShouldRejectThem() {
            // Arrange
            Integer borrowRequestId = 1;
            BorrowRequest acceptedRequest = new BorrowRequest();
            acceptedRequest.setId(borrowRequestId);
            acceptedRequest.setUserBook(userBook);
            acceptedRequest.setStatus(BookRequestStatus.PENDING);

            BorrowRequest otherPendingRequest = new BorrowRequest();
            otherPendingRequest.setId(2);
            otherPendingRequest.setStatus(BookRequestStatus.PENDING);
            
            when(borrowRequestRepository.findByIdAndUserBookOwnerUsername(borrowRequestId, owner.getUsername()))
                    .thenReturn(Optional.of(acceptedRequest));
            when(borrowRequestRepository.findByUserBookIdAndStatusAndIdNot(
                    userBook.getId(), BookRequestStatus.PENDING, borrowRequestId))
                    .thenReturn(List.of(otherPendingRequest));

            // Act
            borrowService.acceptBorrowRequest(owner, borrowRequestId);

            // Assert
            assertEquals(BookRequestStatus.REJECTED, otherPendingRequest.getStatus());
            verify(borrowRequestRepository).saveAll(List.of(otherPendingRequest));
        }

        @Test
        @DisplayName("Should throw exception when borrow request not found")
        void acceptBorrowRequest_WhenNotFound_ShouldThrowException() {
            // Arrange
            Integer borrowRequestId = 999;
            when(borrowRequestRepository.findByIdAndUserBookOwnerUsername(borrowRequestId, owner.getUsername()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    EntityNotFoundException.class,
                    () -> borrowService.acceptBorrowRequest(owner, borrowRequestId)
            );
        }
    }

    @Nested
    @DisplayName("rejectBorrowRequest tests")
    class RejectBorrowRequestTests {

        @Test
        @DisplayName("Should reject borrow request successfully")
        void rejectBorrowRequest_WhenValid_ShouldRejectRequest() {
            // Arrange
            Integer borrowRequestId = 1;
            BorrowRequest borrowRequest = new BorrowRequest();
            borrowRequest.setId(borrowRequestId);
            borrowRequest.setStatus(BookRequestStatus.PENDING);
            
            when(borrowRequestRepository.findByIdAndUserBookOwnerUsername(borrowRequestId, owner.getUsername()))
                    .thenReturn(Optional.of(borrowRequest));

            // Act
            borrowService.rejectBorrowRequest(owner, borrowRequestId);

            // Assert
            assertEquals(BookRequestStatus.REJECTED, borrowRequest.getStatus());
            verify(borrowRequestRepository).save(borrowRequest);
        }

        @Test
        @DisplayName("Should throw exception when borrow request not found for rejection")
        void rejectBorrowRequest_WhenNotFound_ShouldThrowException() {
            // Arrange
            Integer borrowRequestId = 999;
            when(borrowRequestRepository.findByIdAndUserBookOwnerUsername(borrowRequestId, owner.getUsername()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    EntityNotFoundException.class,
                    () -> borrowService.rejectBorrowRequest(owner, borrowRequestId)
            );
        }
    }

    @Nested
    @DisplayName("getIncomingRequests tests")
    class GetIncomingRequestsTests {

        @Test
        @DisplayName("Should return paginated incoming requests")
        void getIncomingRequests_WhenRequestsExist_ShouldReturnPaginatedResult() {
            // Arrange
            BorrowRequest borrowRequest = new BorrowRequest();
            borrowRequest.setId(1);
            borrowRequest.setUserBook(userBook);
            borrowRequest.setBorrower(borrower);
            borrowRequest.setStatus(BookRequestStatus.PENDING);
            borrowRequest.setCreatedAt(LocalDateTime.now());
            borrowRequest.setMeetingTime(LocalDateTime.now().plusDays(1));
            borrowRequest.setDueDate(LocalDate.now().plusWeeks(2));
            borrowRequest.setLocation("Library");

            Page<BorrowRequest> page = new PageImpl<>(List.of(borrowRequest));
            when(borrowRequestRepository.findByUserBookOwnerUsernameAndStatus(
                    eq(owner.getUsername()), eq(BookRequestStatus.PENDING), any(Pageable.class)))
                    .thenReturn(page);

            // Act
            PaginatedResultDTO<BorrowRequestResponseDTO> result = 
                    borrowService.getIncomingRequests(owner, 10, 1);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalCount());
            assertFalse(result.getItems().isEmpty());
        }

        @Test
        @DisplayName("Should return empty result when no incoming requests")
        void getIncomingRequests_WhenNoRequests_ShouldReturnEmptyResult() {
            // Arrange
            Page<BorrowRequest> emptyPage = new PageImpl<>(Collections.emptyList());
            when(borrowRequestRepository.findByUserBookOwnerUsernameAndStatus(
                    eq(owner.getUsername()), eq(BookRequestStatus.PENDING), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Act
            PaginatedResultDTO<BorrowRequestResponseDTO> result = 
                    borrowService.getIncomingRequests(owner, 10, 1);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalCount());
            assertTrue(result.getItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("getOutgoingRequests tests")
    class GetOutgoingRequestsTests {

        @Test
        @DisplayName("Should return outgoing requests for borrower")
        void getOutgoingRequests_WhenRequestsExist_ShouldReturnList() {
            // Arrange
            BorrowRequest borrowRequest = new BorrowRequest();
            borrowRequest.setId(1);
            borrowRequest.setUserBook(userBook);
            borrowRequest.setBorrower(borrower);
            borrowRequest.setStatus(BookRequestStatus.PENDING);
            borrowRequest.setCreatedAt(LocalDateTime.now());
            borrowRequest.setMeetingTime(LocalDateTime.now().plusDays(1));
            borrowRequest.setDueDate(LocalDate.now().plusWeeks(2));
            borrowRequest.setLocation("Library");

            when(borrowRequestRepository.findByBorrowerUsernameAndStatus(
                    borrower.getUsername(), BookRequestStatus.PENDING))
                    .thenReturn(List.of(borrowRequest));

            // Act
            List<BorrowRequestResponseDTO> result = borrowService.getOutgoingRequests(borrower);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return empty list when no outgoing requests")
        void getOutgoingRequests_WhenNoRequests_ShouldReturnEmptyList() {
            // Arrange
            when(borrowRequestRepository.findByBorrowerUsernameAndStatus(
                    borrower.getUsername(), BookRequestStatus.PENDING))
                    .thenReturn(Collections.emptyList());

            // Act
            List<BorrowRequestResponseDTO> result = borrowService.getOutgoingRequests(borrower);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Admin operations tests")
    class AdminOperationsTests {

        private User admin;

        @BeforeEach
        void setUpAdmin() {
            admin = User.builder()
                    .id(3)
                    .username("admin")
                    .email("admin@test.com")
                    .role(Role.ADMIN)
                    .build();
        }

        @Test
        @DisplayName("Should throw SecurityException when non-admin tries admin accept")
        void adminAcceptBorrowRequest_WhenNotAdmin_ShouldThrowSecurityException() {
            // Arrange
            User regularUser = User.builder()
                    .id(1)
                    .username("user")
                    .role(Role.USER)
                    .build();

            // Act & Assert
            assertThrows(
                    SecurityException.class,
                    () -> borrowService.adminAcceptBorrowRequest(regularUser, 1)
            );
        }

        @Test
        @DisplayName("Should throw SecurityException when non-admin tries admin reject")
        void adminRejectBorrowRequest_WhenNotAdmin_ShouldThrowSecurityException() {
            // Arrange
            User regularUser = User.builder()
                    .id(1)
                    .username("user")
                    .role(Role.USER)
                    .build();

            // Act & Assert
            assertThrows(
                    SecurityException.class,
                    () -> borrowService.adminRejectBorrowRequest(regularUser, 1)
            );
        }
    }
}
