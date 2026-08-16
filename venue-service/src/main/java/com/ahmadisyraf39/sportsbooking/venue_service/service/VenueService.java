package com.ahmadisyraf39.sportsbooking.venue_service.service;

import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.VenueCreateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.VenueUpdateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.response.VenueResponse;
import com.ahmadisyraf39.sportsbooking.venue_service.entity.Venue;
import com.ahmadisyraf39.sportsbooking.venue_service.exception.UnauthorizedOperationException;
import com.ahmadisyraf39.sportsbooking.venue_service.exception.VenueNotFoundException;
import com.ahmadisyraf39.sportsbooking.venue_service.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    public List<VenueResponse> listVenues(String search) {
        List<Venue> venues = (search == null || search.isBlank())
                ? venueRepository.findAll()
                : venueRepository.findByNameContainingIgnoreCase(search);

        return venues.stream()
                .map(this::toVenueResponse)
                .toList();
    }

    public VenueResponse getVenue(Long id) {
        return toVenueResponse(findVenueById(id));
    }

    public VenueResponse createVenue(VenueCreateRequest request) {
        Venue venue = new Venue();
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setDescription(request.getDescription());
        venue.setOpeningTime(request.getOpeningTime());
        venue.setClosingTime(request.getClosingTime());
        venue.setOwnerId(request.getOwnerId());

        return toVenueResponse(venueRepository.save(venue));
    }

    public VenueResponse updateVenue(Long id, VenueUpdateRequest request) {
        Venue venue = findVenueById(id);
        verifyOwnership(venue.getOwnerId(), request.getOwnerId());

        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setDescription(request.getDescription());
        venue.setOpeningTime(request.getOpeningTime());
        venue.setClosingTime(request.getClosingTime());

        return toVenueResponse(venueRepository.save(venue));
    }

    public void deleteVenue(Long id, Long ownerId) {
        Venue venue = findVenueById(id);
        verifyOwnership(venue.getOwnerId(), ownerId);

        venueRepository.delete(venue);
    }

    Venue findVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found with id: " + id));
    }

    private void verifyOwnership(Long actualOwnerId, Long requestOwnerId) {
        if (!actualOwnerId.equals(requestOwnerId)) {
            throw new UnauthorizedOperationException("You do not have permission to modify this venue");
        }
    }

    private VenueResponse toVenueResponse(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .description(venue.getDescription())
                .openingTime(venue.getOpeningTime())
                .closingTime(venue.getClosingTime())
                .ownerId(venue.getOwnerId())
                .build();
    }
}
