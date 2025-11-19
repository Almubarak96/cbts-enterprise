package com.almubaraksuleiman.cbts.mail.exception;

/** Simple runtime exception to indicate missing resource (404-like). */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
