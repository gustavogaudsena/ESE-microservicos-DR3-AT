package br.com.infnet.userCredits.controller;

import br.com.infnet.userCredits.dto.CreditRequest;
import br.com.infnet.userCredits.model.Credit;
import br.com.infnet.userCredits.repository.CreditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class CreditControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("credits")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CreditRepository creditRepository;

    private UUID userId;

    @DynamicPropertySource
    static void configureDb(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                String.format("r2dbc:postgresql://%s:%d/%s",
                        postgres.getHost(),
                        postgres.getMappedPort(5432),
                        postgres.getDatabaseName()
                ));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @BeforeEach
    void setup() {
        creditRepository.deleteAll().block();
        userId = UUID.randomUUID();
    }

    @Test
    void endPointShouldReturnCreditAddedToUser() {
        CreditRequest req = new CreditRequest(userId, 10);

        webTestClient.post()
                .uri("/")
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId.toString())
                .jsonPath("$.credit").isEqualTo(10);
    }

    @Test
    void endPointShouldReturnCreditFromUser() {
        Credit c = Credit.builder()
                .userId(userId)
                .credit(20)
                .build();

        creditRepository.save(c).block();

        webTestClient.get()
                .uri("/" + userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.credit").isEqualTo(20);
    }

    @Test
    void endPointShouldReturnConsumedCreditBalanceFromUser() {
        Credit c = Credit.builder()
                .userId(userId)
                .credit(5)
                .build();

        creditRepository.save(c).block();

        webTestClient.put()
                .uri("/" + userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.credit").isEqualTo(4);
    }

    @Test
    void endPointShouldReturnUserNotFound() {
        webTestClient.get()
                .uri("/" + userId)
                .exchange()
                .expectStatus().isNotFound();
    }
}
