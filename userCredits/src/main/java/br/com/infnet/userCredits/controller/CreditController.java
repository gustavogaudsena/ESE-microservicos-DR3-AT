package br.com.infnet.userCredits.controller;

import br.com.infnet.userCredits.dto.CreditRequest;
import br.com.infnet.userCredits.dto.CreditResponse;
import br.com.infnet.userCredits.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreditResponse> addCredit(@RequestBody CreditRequest request) {
        return creditService.addCredit(request);
    }

    @GetMapping("/{userId}")
    public Mono<CreditResponse> get(@PathVariable UUID userId) {
        return creditService.getCredit(userId);
    }

    @PutMapping("/{userId}")
    public Mono<CreditResponse> useCredit(@PathVariable UUID userId) {
        return creditService.useCredit(userId);
    }
}