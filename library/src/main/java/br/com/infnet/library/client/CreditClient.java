package br.com.infnet.library.client;

import br.com.infnet.library.dto.CreditResponse;
import br.com.infnet.library.exception.NoCreditAvailableException;
import br.com.infnet.library.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${credit.service.url:http://localhost:8082/credit}")
    private String creditServiceUrl;

    public Mono<CreditResponse> useCredit(UUID userId) {
        return webClientBuilder.build()
                .put()
                .uri(creditServiceUrl + "/{userId}", userId)
                .retrieve()
                .bodyToMono(CreditResponse.class)
                .onErrorMap(WebClientResponseException.BadRequest.class, ex ->
                    new NoCreditAvailableException()
                )
                .onErrorMap(WebClientResponseException.NotFound.class, ex ->
                    new UserNotFoundException(userId)
                );
    }

}
