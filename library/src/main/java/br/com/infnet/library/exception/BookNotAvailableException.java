package br.com.infnet.library.exception;

public class BookNotAvailableException extends RuntimeException {
    public BookNotAvailableException() {
        super("Book is not available in the moment");
    }
}
