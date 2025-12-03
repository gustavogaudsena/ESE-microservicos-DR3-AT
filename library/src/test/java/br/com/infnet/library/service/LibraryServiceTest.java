package br.com.infnet.library.service;

import br.com.infnet.library.client.CreditClient;
import br.com.infnet.library.dto.CreditResponse;
import br.com.infnet.library.dto.CreateLoanRequest;
import br.com.infnet.library.dto.CreateLoanResponse;
import br.com.infnet.library.exception.BookNotAvailableException;
import br.com.infnet.library.exception.NoCreditAvailableException;
import br.com.infnet.library.model.Loan;
import br.com.infnet.library.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CreditClient creditClient;

    @InjectMocks
    private LibraryService libraryService;

    private UUID userId;
    private UUID bookId;
    private CreateLoanRequest request;
    private Loan loan;
    private CreditResponse creditResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        request = new CreateLoanRequest(userId, bookId);

        loan = Loan.builder()
                .id(1)
                .userId(userId)
                .bookId(bookId)
                .lendAt(Instant.now())
                .dueDate(Instant.now().plusSeconds(7 * 24 * 60 * 60))
                .status(Loan.LoanStatus.ACTIVE)
                .build();

        creditResponse = new CreditResponse(userId, 5);
    }

    @Test
    void createLoanShouldCreateLoanWhenHaveCredits() {
        when(loanRepository.findByBookIdAndStatus(eq(bookId), eq(Loan.LoanStatus.ACTIVE)))
                .thenReturn(Mono.empty());
        when(creditClient.useCredit(userId)).thenReturn(Mono.just(creditResponse));
        when(loanRepository.save(any(Loan.class))).thenReturn(Mono.just(loan));

        Mono<CreateLoanResponse> result = libraryService.createLoan(request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getUserId()).isEqualTo(userId);
                    assertThat(response.getBookId()).isEqualTo(bookId);
                    assertThat(response.getStatus()).isEqualTo(Loan.LoanStatus.ACTIVE);
                })
                .verifyComplete();
    }

    @Test
    void createLoanShouldThrowExceptionWhenBookIsNotAvailable() {
        Loan activeLoan = Loan.builder()
                .id(1)
                .bookId(bookId)
                .status(Loan.LoanStatus.ACTIVE)
                .build();

        when(loanRepository.findByBookIdAndStatus(eq(bookId), eq(Loan.LoanStatus.ACTIVE)))
                .thenReturn(Mono.just(activeLoan));

        Mono<CreateLoanResponse> result = libraryService.createLoan(request);

        StepVerifier.create(result)
                .expectError(BookNotAvailableException.class)
                .verify();
    }

    @Test
    void createLoanShouldThrowExceptionWhenWhenNoCreditAvailable() {
        when(loanRepository.findByBookIdAndStatus(eq(bookId), eq(Loan.LoanStatus.ACTIVE)))
                .thenReturn(Mono.empty());
        when(creditClient.useCredit(userId))
                .thenReturn(Mono.error(new NoCreditAvailableException()));

        Mono<CreateLoanResponse> result = libraryService.createLoan(request);

        StepVerifier.create(result)
                .expectError(NoCreditAvailableException.class)
                .verify();
    }

    @Test
    void getLoansByUserIdShouldReturnAllLoansForUser() {
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

        when(loanRepository.findAllByUserId(userId))
                .thenReturn(Flux.just(loan1, loan2));

        Flux<Loan> result = libraryService.getLoansByUserId(userId);

        StepVerifier.create(result)
                .assertNext(loan -> assertThat(loan.getId()).isEqualTo(1))
                .assertNext(loan -> assertThat(loan.getId()).isEqualTo(2))
                .verifyComplete();
    }

    @Test
    void getLoansByUserIdShouldReturnEmpty() {
        when(loanRepository.findAllByUserId(userId))
                .thenReturn(Flux.empty());

        Flux<Loan> result = libraryService.getLoansByUserId(userId);

        StepVerifier.create(result)
                .verifyComplete();
    }
}
