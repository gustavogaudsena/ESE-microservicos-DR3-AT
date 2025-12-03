package br.com.infnet.userCredits.exception;


public class NoCreditAvailableException extends RuntimeException {
    public NoCreditAvailableException() {
        super("User has no more credits");
    }
}