package br.com.infnet.userCredits.exception;

public class InvalidCreditValueException extends RuntimeException {
    public InvalidCreditValueException(Integer value) {
        super("Credit value must be greater than 0. Received: " + value);
    }
}