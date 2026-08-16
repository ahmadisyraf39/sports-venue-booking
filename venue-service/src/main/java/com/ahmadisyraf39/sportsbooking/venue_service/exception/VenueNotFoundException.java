package com.ahmadisyraf39.sportsbooking.venue_service.exception;

public class VenueNotFoundException extends RuntimeException {
    public VenueNotFoundException(String message) {
        super(message);
    }
}
