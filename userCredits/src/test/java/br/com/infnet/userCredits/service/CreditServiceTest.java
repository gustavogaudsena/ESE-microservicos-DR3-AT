package br.com.infnet.userCredits.service;

import br.com.infnet.userCredits.dto.CreditRequest;
import br.com.infnet.userCredits.exception.InvalidCreditValueException;
import br.com.infnet.userCredits.exception.NoCreditAvailableException;
import br.com.infnet.userCredits.exception.UserNotFoundException;
import br.com.infnet.userCredits.model.Credit;
import br.com.infnet.userCredits.repository.CreditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.UUID;

@SpringBootTest
@Testcontainers
class CreditServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("credits")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private CreditService creditService;

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
    void addCreditShouldReturnCreditToNewUser() {
        CreditRequest request = new CreditRequest(userId, 10);

        StepVerifier.create(creditService.addCredit(request))
                .expectNextMatches(response ->
                        response.getUserId().equals(userId) &&
                        response.getCredit().equals(10))
                .verifyComplete();
    }

    @Test
    void addCreditShouldReturnCreditToExistingUser() {
        Credit credit = Credit.builder()
                .userId(userId)
                .credit(20)
                .build();
        creditRepository.save(credit).block();

        CreditRequest request = new CreditRequest(userId, 10);

        StepVerifier.create(creditService.addCredit(request))
                .expectNextMatches(response ->
                        response.getUserId().equals(userId) &&
                        response.getCredit().equals(30))
                .verifyComplete();
    }

    @Test
    void addCreditShouldThrowExceptionWhenAddingInvalidCreditValue() {
        CreditRequest request = new CreditRequest(userId, -10);

        StepVerifier.create(creditService.addCredit(request))
                .expectError(InvalidCreditValueException.class)
                .verify();
    }

    @Test
    void addCreditShouldThrowExceptionWhenAddingZeroCredit() {
        CreditRequest request = new CreditRequest(userId, 0);

        StepVerifier.create(creditService.addCredit(request))
                .expectError(InvalidCreditValueException.class)
                .verify();
    }

    @Test
    void getCreditShouldReturnCreditsOfUser() {
        Credit credit = Credit.builder()
                .userId(userId)
                .credit(50)
                .build();
        creditRepository.save(credit).block();

        StepVerifier.create(creditService.getCredit(userId))
                .expectNextMatches(response ->
                        response.getUserId().equals(userId) &&
                        response.getCredit().equals(50))
                .verifyComplete();
    }

    @Test
    void getCreditShouldThrowExceptionWhenUserNotFound() {
        StepVerifier.create(creditService.getCredit(userId))
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    void useCreditShouldReturnConsumedCreditBalanceFromUser() {
        Credit credit = Credit.builder()
                .userId(userId)
                .credit(10)
                .build();
        creditRepository.save(credit).block();

        StepVerifier.create(creditService.useCredit(userId))
                .expectNextMatches(response ->
                        response.getUserId().equals(userId) &&
                        response.getCredit().equals(9))
                .verifyComplete();
    }

    @Test
    void useCreditShouldThrowExceptionWhenNoCreditAvailable() {
        Credit credit = Credit.builder()
                .userId(userId)
                .credit(0)
                .build();
        creditRepository.save(credit).block();

        StepVerifier.create(creditService.useCredit(userId))
                .expectError(NoCreditAvailableException.class)
                .verify();
    }

    @Test
    void useCreditShouldThrowExceptionWhenUsingCreditForNonExistentUser() {
        StepVerifier.create(creditService.useCredit(userId))
                .expectError(UserNotFoundException.class)
                .verify();
    }
}
