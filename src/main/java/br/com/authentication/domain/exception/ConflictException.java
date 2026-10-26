package br.com.authentication.domain.exception;

/** The operation conflicts with existing data (e.g. duplicated email or claim name). Mapped to HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
