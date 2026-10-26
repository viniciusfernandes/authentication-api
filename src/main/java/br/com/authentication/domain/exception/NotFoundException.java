package br.com.authentication.domain.exception;

/** A referenced resource (user, claim, ...) does not exist. Mapped to HTTP 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
