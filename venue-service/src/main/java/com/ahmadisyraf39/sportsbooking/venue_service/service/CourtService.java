package com.ahmadisyraf39.sportsbooking.venue_service.service;

import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.CourtCreateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.CourtUpdateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.response.CourtResponse;
import com.ahmadisyraf39.sportsbooking.venue_service.entity.Court;
import com.ahmadisyraf39.sportsbooking.venue_service.entity.Venue;
import com.ahmadisyraf39.sportsbooking.venue_service.exception.CourtNotFoundException;
import com.ahmadisyraf39.sportsbooking.venue_service.exception.UnauthorizedOperationException;
import com.ahmadisyraf39.sportsbooking.venue_service.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService {

    private final CourtRepository courtRepository;
    private final VenueService venueService;

    public List<CourtResponse> listCourtsForVenue(Long venueId) {
        venueService.findVenueById(venueId);

        return courtRepository.findByVenueId(venueId).stream()
                .map(this::toCourtResponse)
                .toList();
    }

    public CourtResponse addCourt(Long venueId, CourtCreateRequest request) {
        Venue venue = venueService.findVenueById(venueId);
        verifyOwnership(venue.getOwnerId(), request.getOwnerId());

        Court court = new Court();
        court.setVenue(venue);
        court.setName(request.getName());
        court.setSportType(request.getSportType());
        court.setHourlyPrice(request.getHourlyPrice());

        return toCourtResponse(courtRepository.save(court));
    }

    public CourtResponse updateCourt(Long courtId, CourtUpdateRequest request) {
        Court court = findCourtById(courtId);
        verifyOwnership(court.getVenue().getOwnerId(), request.getOwnerId());

        court.setName(request.getName());
        court.setSportType(request.getSportType());
        court.setHourlyPrice(request.getHourlyPrice());

        return toCourtResponse(courtRepository.save(court));
    }

    public void deleteCourt(Long courtId, Long ownerId) {
        Court court = findCourtById(courtId);
        verifyOwnership(court.getVenue().getOwnerId(), ownerId);

        courtRepository.delete(court);
    }

    private Court findCourtById(Long id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new CourtNotFoundException("Court not found with id: " + id));
    }

    private void verifyOwnership(Long actualOwnerId, Long requestOwnerId) {
        if (!actualOwnerId.equals(requestOwnerId)) {
            throw new UnauthorizedOperationException("You do not have permission to modify this court");
        }
    }

    private CourtResponse toCourtResponse(Court court) {
        return CourtResponse.builder()
                .id(court.getId())
                .venueId(court.getVenue().getId())
                .name(court.getName())
                .sportType(court.getSportType())
                .hourlyPrice(court.getHourlyPrice())
                .build();
    }
}
