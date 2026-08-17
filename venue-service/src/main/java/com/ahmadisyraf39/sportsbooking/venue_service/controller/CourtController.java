package com.ahmadisyraf39.sportsbooking.venue_service.controller;

import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.CourtUpdateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.response.CourtResponse;
import com.ahmadisyraf39.sportsbooking.venue_service.service.CourtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping("/{id}")
    public ResponseEntity<CourtResponse> getCourt(@PathVariable Long id) {
        return ResponseEntity.ok(courtService.getCourtById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourtResponse> updateCourt(@PathVariable Long id,
                                                       @Valid @RequestBody CourtUpdateRequest request) {
        return ResponseEntity.ok(courtService.updateCourt(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourt(@PathVariable Long id, @RequestParam Long ownerId) {
        courtService.deleteCourt(id, ownerId);
        return ResponseEntity.noContent().build();
    }
}
