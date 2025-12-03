package br.com.infnet.userCredits.repository;

import br.com.infnet.userCredits.model.Credit;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Repository
public interface CreditRepository extends ReactiveCrudRepository<Credit, Integer> {
    Mono<Credit> findByUserId(UUID userId);
}


