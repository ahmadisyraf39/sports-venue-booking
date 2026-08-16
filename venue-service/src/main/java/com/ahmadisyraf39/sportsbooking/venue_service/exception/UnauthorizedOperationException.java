package com.ahmadisyraf39.sportsbooking.venue_service.exception;

public class UnauthorizedOperationException extends RuntimeException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
