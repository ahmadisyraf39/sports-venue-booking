package com.ahmadisyraf39.sportsbooking.booking_service.exception;

public class SlotUnavailableException extends RuntimeException {
    public SlotUnavailableException(String message) {
        super(message);
    }
}
