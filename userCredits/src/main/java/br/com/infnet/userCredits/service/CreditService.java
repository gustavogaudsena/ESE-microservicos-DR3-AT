package br.com.infnet.userCredits.service;

import br.com.infnet.userCredits.dto.CreditRequest;
import br.com.infnet.userCredits.dto.CreditResponse;
import br.com.infnet.userCredits.exception.UserNotFoundException;
import br.com.infnet.userCredits.model.Credit;
import br.com.infnet.userCredits.repository.CreditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditService {

    private final CreditRepository creditRepository;

    public Mono<CreditResponse> addCredit(CreditRequest dto) {
        return creditRepository.findByUserId(dto.getUserId())
                .switchIfEmpty(Mono.fromSupplier(() -> Credit.builder().userId(dto.getUserId()).credit(0).build()))
                .flatMap(c -> {
                    c.addCredit(dto.getValue());
                    return creditRepository.save(c);
                })
                .map(this::toResponse);
    }

    public Mono<CreditResponse> getCredit(UUID userId) {
        return creditRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .map(this::toResponse);
    }

    public Mono<CreditResponse> useCredit(UUID userId) {
        return creditRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .flatMap(credit -> {
                    try {
                        credit.useCredit();
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                    return creditRepository.save(credit);
                })
                .map(this::toResponse);
    }

    private CreditResponse toResponse(Credit credit) {
        return new CreditResponse(
                credit.getUserId(),
                credit.getCredit(),
                credit.getRenewAt() != null ? credit.getRenewAt().toString() : null
        );
    }
}
