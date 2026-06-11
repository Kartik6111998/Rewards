package com.rewards.exception;

/**
 * Thrown when a requested customer ID has no transaction history in the system.
 * Results in a 404 Not Found HTTP response via GlobalExceptionHandler.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
