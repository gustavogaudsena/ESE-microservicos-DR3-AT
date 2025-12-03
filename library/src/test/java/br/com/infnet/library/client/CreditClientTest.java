package br.com.infnet.library.client;

import br.com.infnet.library.dto.CreditResponse;
import br.com.infnet.library.exception.NoCreditAvailableException;
import br.com.infnet.library.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private CreditClient creditClient;
    private UUID userId;

    @BeforeEach
    void setUp() {
        creditClient = new CreditClient(webClientBuilder);
        userId = UUID.randomUUID();
        ReflectionTestUtils.setField(creditClient, "creditServiceUrl", "http://localhost:8082/credit");

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), any(UUID.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void useCreditShouldReturnCreditResponse() {
        CreditResponse expectedResponse = new CreditResponse(userId, 5);
        when(responseSpec.bodyToMono(CreditResponse.class)).thenReturn(Mono.just(expectedResponse));

        Mono<CreditResponse> result = creditClient.useCredit(userId);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getUserId().equals(userId) &&
                        response.getAvailableCredits().equals(5)
                )
                .verifyComplete();
    }

    @Test
    void useCreditShouldThrowNoCreditAvailableException() {
        WebClientResponseException badRequestException =
            WebClientResponseException.create(400, "Bad Request", null, null, null);

        when(responseSpec.bodyToMono(CreditResponse.class))
            .thenReturn(Mono.error(badRequestException));

        Mono<CreditResponse> result = creditClient.useCredit(userId);

        StepVerifier.create(result)
                .expectError(NoCreditAvailableException.class)
                .verify();
    }

    @Test
    void useCreditShouldThrowUserNotFoundException() {
        WebClientResponseException notFoundException =
            WebClientResponseException.create(404, "Not Found", null, null, null);

        when(responseSpec.bodyToMono(CreditResponse.class))
            .thenReturn(Mono.error(notFoundException));

        Mono<CreditResponse> result = creditClient.useCredit(userId);

        StepVerifier.create(result)
                .expectError(UserNotFoundException.class)
                .verify();
    }
}
