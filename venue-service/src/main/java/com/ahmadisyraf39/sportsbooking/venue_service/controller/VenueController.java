package com.ahmadisyraf39.sportsbooking.venue_service.controller;

import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.CourtCreateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.VenueCreateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.VenueUpdateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.response.CourtResponse;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.response.VenueResponse;
import com.ahmadisyraf39.sportsbooking.venue_service.service.CourtService;
import com.ahmadisyraf39.sportsbooking.venue_service.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;
    private final CourtService courtService;

    @GetMapping
    public ResponseEntity<List<VenueResponse>> listVenues(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(venueService.listVenues(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> getVenue(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.getVenue(id));
    }

    @GetMapping("/{id}/courts")
    public ResponseEntity<List<CourtResponse>> listCourtsForVenue(@PathVariable Long id) {
        return ResponseEntity.ok(courtService.listCourtsForVenue(id));
    }

    @PostMapping
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody VenueCreateRequest request) {
        VenueResponse response = venueService.createVenue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VenueResponse> updateVenue(@PathVariable Long id,
                                                       @Valid @RequestBody VenueUpdateRequest request) {
        return ResponseEntity.ok(venueService.updateVenue(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id, @RequestParam Long ownerId) {
        venueService.deleteVenue(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/courts")
    public ResponseEntity<CourtResponse> addCourt(@PathVariable Long id,
                                                    @Valid @RequestBody CourtCreateRequest request) {
        CourtResponse response = courtService.addCourt(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
