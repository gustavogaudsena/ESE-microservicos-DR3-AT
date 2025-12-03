package br.com.infnet.library.controller;

import br.com.infnet.library.dto.CreateLoanRequest;
import br.com.infnet.library.dto.CreateLoanResponse;
import br.com.infnet.library.model.Loan;
import br.com.infnet.library.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Loan> getLoansByUserId(@PathVariable UUID userId) {
        return libraryService.getLoansByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreateLoanResponse> createLoan(@RequestBody CreateLoanRequest request) {
        return libraryService.createLoan(request);
    }

}
