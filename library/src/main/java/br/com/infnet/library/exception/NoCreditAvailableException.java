package br.com.infnet.library.exception;


public class NoCreditAvailableException extends RuntimeException {
    public NoCreditAvailableException() {
        super("User has no more credits");
    }
}