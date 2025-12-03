package br.com.infnet.library.repository;

import br.com.infnet.library.model.Loan;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface LoanRepository extends ReactiveCrudRepository<Loan, Integer> {

    Mono<Loan> findByBookIdAndStatus(UUID bookId, Loan.LoanStatus status);
    Flux<Loan> findAllByUserIdAndStatus(UUID userId, Loan.LoanStatus status);
    Flux<Loan> findAllByUserId(UUID userId);
}
