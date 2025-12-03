package br.com.infnet.library.service;

import br.com.infnet.library.client.CreditClient;
import br.com.infnet.library.dto.CreateLoanRequest;
import br.com.infnet.library.dto.CreateLoanResponse;
import br.com.infnet.library.exception.BookNotAvailableException;
import br.com.infnet.library.model.Loan;
import br.com.infnet.library.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryService {

    private final LoanRepository loanRepository;
    private final CreditClient creditClient;

    public Mono<CreateLoanResponse> createLoan(CreateLoanRequest dto) {
        return isBookAvailable(dto.getBookId())
                .flatMap(available -> {
                    if (!available) {
                        return Mono.error(new BookNotAvailableException());
                    }
                    return creditClient.useCredit(dto.getUserId());
                })
                .flatMap(creditResponse -> {
                    Loan newLoan = Loan.builder()
                            .userId(dto.getUserId())
                            .bookId(dto.getBookId())
                            .lendAt(Instant.now())
                            .dueDate(Instant.now().plusSeconds(7 * 24 * 60 * 60)) // 7 dias
                            .status(Loan.LoanStatus.ACTIVE)
                            .build();

                    return loanRepository.save(newLoan);
                })
                .map(this::toResponse);
    }

    private Mono<Boolean> isBookAvailable(UUID bookId) {
        return loanRepository.findByBookIdAndStatus(bookId, Loan.LoanStatus.ACTIVE)
                .map(loan -> false)
                .defaultIfEmpty(true);
    }

    public Flux<Loan> getLoansByUserId(UUID userId) {
        return loanRepository.findAllByUserId(userId);
    }

    private CreateLoanResponse toResponse(Loan loan) {
        return new CreateLoanResponse(
                loan.getUserId(),
                loan.getBookId(),
                loan.getDueDate().toString(),
                loan.getStatus()
        );
    }
}
