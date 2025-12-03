package br.com.infnet.library.controller;

import br.com.infnet.library.dto.CreateLoanRequest;
import br.com.infnet.library.dto.CreateLoanResponse;
import br.com.infnet.library.exception.BookNotAvailableException;
import br.com.infnet.library.exception.NoCreditAvailableException;
import br.com.infnet.library.exception.UserNotFoundException;
import br.com.infnet.library.model.Loan;
import br.com.infnet.library.service.LibraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(LibraryController.class)
class LibraryControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private LibraryService libraryService;

    private UUID userId;
    private UUID bookId;
    private CreateLoanRequest request;
    private CreateLoanResponse response;
    private Loan loan;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        request = new CreateLoanRequest(userId, bookId);

        response = new CreateLoanResponse(
                userId,
                bookId,
                Instant.now().plusSeconds(7 * 24 * 60 * 60).toString(),
                Loan.LoanStatus.ACTIVE
        );

        loan = Loan.builder()
                .id(1)
                .userId(userId)
                .bookId(bookId)
                .lendAt(Instant.now())
                .dueDate(Instant.now().plusSeconds(7 * 24 * 60 * 60))
                .status(Loan.LoanStatus.ACTIVE)
                .build();
    }

    @Test
    void endPointShouldCreateLoan() {
        when(libraryService.createLoan(any(CreateLoanRequest.class)))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId.toString())
                .jsonPath("$.bookId").isEqualTo(bookId.toString())
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test
    void endPointShouldThrowExceptionWhenBookIsNotAvailable() {
        when(libraryService.createLoan(any(CreateLoanRequest.class)))
                .thenReturn(Mono.error(new BookNotAvailableException()));

        webTestClient.post()
                .uri("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").exists()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void endPointShouldThrowExceptionWhenWhenNoCreditAvailable() {
        when(libraryService.createLoan(any(CreateLoanRequest.class)))
                .thenReturn(Mono.error(new NoCreditAvailableException()));

        webTestClient.post()
                .uri("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("User has no more credits")
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void endPointShouldThrowExceptionWhenWhenUserNotFound() {
        when(libraryService.createLoan(any(CreateLoanRequest.class)))
                .thenReturn(Mono.error(new UserNotFoundException(userId)));

        webTestClient.post()
                .uri("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("User not found: " + userId)
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void endPointShouldReturnUserLoans() {
        Loan loan1 = Loan.builder()
                .id(1)
                .userId(userId)
                .bookId(UUID.randomUUID())
                .status(Loan.LoanStatus.ACTIVE)
                .build();

        Loan loan2 = Loan.builder()
                .id(2)
                .userId(userId)
                .bookId(UUID.randomUUID())
                .status(Loan.LoanStatus.RETURNED)
                .build();

        when(libraryService.getLoansByUserId(eq(userId)))
                .thenReturn(Flux.just(loan1, loan2));

        webTestClient.get()
                .uri("/loans/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Loan.class)
                .hasSize(2);
    }

    @Test
    void endPointShouldReturnEmptyListUserLoans() {
        when(libraryService.getLoansByUserId(eq(userId)))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/loans/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Loan.class)
                .hasSize(0);
    }
}
