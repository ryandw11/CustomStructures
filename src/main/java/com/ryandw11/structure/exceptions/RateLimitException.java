package com.ryandw11.structure.exceptions;

/**
 * This exception occurs when an API method is rate limited due to performance concerns.
 */
public class RateLimitException extends RuntimeException{
    public RateLimitException(String message) {
        super(message);
    }
}
