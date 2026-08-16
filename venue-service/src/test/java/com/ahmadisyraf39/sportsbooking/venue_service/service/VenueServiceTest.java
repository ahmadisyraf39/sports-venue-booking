package com.ahmadisyraf39.sportsbooking.venue_service.service;

import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.VenueCreateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.VenueUpdateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.response.VenueResponse;
import com.ahmadisyraf39.sportsbooking.venue_service.entity.Venue;
import com.ahmadisyraf39.sportsbooking.venue_service.exception.UnauthorizedOperationException;
import com.ahmadisyraf39.sportsbooking.venue_service.exception.VenueNotFoundException;
import com.ahmadisyraf39.sportsbooking.venue_service.repository.VenueRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueService venueService;

    private Venue existingVenue() {
        Venue venue = new Venue();
        venue.setId(1L);
        venue.setName("Downtown Sports Hall");
        venue.setAddress("123 Main St");
        venue.setOpeningTime(LocalTime.of(8, 0));
        venue.setClosingTime(LocalTime.of(22, 0));
        venue.setOwnerId(10L);
        return venue;
    }

    @Nested
    class CreateVenue {

        @Test
        void shouldCreateVenue() {
            VenueCreateRequest request = new VenueCreateRequest();
            request.setName("Downtown Sports Hall");
            request.setAddress("123 Main St");
            request.setOpeningTime(LocalTime.of(8, 0));
            request.setClosingTime(LocalTime.of(22, 0));
            request.setOwnerId(10L);

            when(venueRepository.save(any(Venue.class))).thenAnswer(invocation -> {
                Venue venue = invocation.getArgument(0);
                venue.setId(1L);
                return venue;
            });

            VenueResponse response = venueService.createVenue(request);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Downtown Sports Hall");
            assertThat(response.getOwnerId()).isEqualTo(10L);
        }
    }

    @Nested
    class GetVenue {

        @Test
        void shouldReturnVenue_WhenExists() {
            when(venueRepository.findById(1L)).thenReturn(Optional.of(existingVenue()));

            VenueResponse response = venueService.getVenue(1L);

            assertThat(response.getId()).isEqualTo(1L);
        }

        @Test
        void shouldThrowException_WhenNotFound() {
            when(venueRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> venueService.getVenue(99L))
                    .isInstanceOf(VenueNotFoundException.class);
        }
    }

    @Nested
    class ListVenues {

        @Test
        void shouldReturnAllVenues_WhenNoSearchTerm() {
            when(venueRepository.findAll()).thenReturn(List.of(existingVenue()));

            List<VenueResponse> responses = venueService.listVenues(null);

            assertThat(responses).hasSize(1);
            verify(venueRepository, never()).findByNameContainingIgnoreCase(anyString());
        }

        @Test
        void shouldSearchByName_WhenSearchTermProvided() {
            when(venueRepository.findByNameContainingIgnoreCase("downtown")).thenReturn(List.of(existingVenue()));

            List<VenueResponse> responses = venueService.listVenues("downtown");

            assertThat(responses).hasSize(1);
            verify(venueRepository, never()).findAll();
        }
    }

    @Nested
    class UpdateVenue {

        @Test
        void shouldUpdateVenue_WhenOwnerIdMatches() {
            Venue venue = existingVenue();
            VenueUpdateRequest request = new VenueUpdateRequest();
            request.setName("Updated Name");
            request.setAddress("456 New St");
            request.setOpeningTime(LocalTime.of(9, 0));
            request.setClosingTime(LocalTime.of(21, 0));
            request.setOwnerId(10L);

            when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
            when(venueRepository.save(any(Venue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            VenueResponse response = venueService.updateVenue(1L, request);

            assertThat(response.getName()).isEqualTo("Updated Name");
        }

        @Test
        void shouldThrowException_WhenOwnerIdDoesNotMatch() {
            Venue venue = existingVenue();
            VenueUpdateRequest request = new VenueUpdateRequest();
            request.setName("Updated Name");
            request.setAddress("456 New St");
            request.setOpeningTime(LocalTime.of(9, 0));
            request.setClosingTime(LocalTime.of(21, 0));
            request.setOwnerId(999L);

            when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

            assertThatThrownBy(() -> venueService.updateVenue(1L, request))
                    .isInstanceOf(UnauthorizedOperationException.class);

            verify(venueRepository, never()).save(any(Venue.class));
        }
    }

    @Nested
    class DeleteVenue {

        @Test
        void shouldDeleteVenue_WhenOwnerIdMatches() {
            Venue venue = existingVenue();
            when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

            venueService.deleteVenue(1L, 10L);

            verify(venueRepository).delete(venue);
        }

        @Test
        void shouldThrowException_WhenOwnerIdDoesNotMatch() {
            Venue venue = existingVenue();
            when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

            assertThatThrownBy(() -> venueService.deleteVenue(1L, 999L))
                    .isInstanceOf(UnauthorizedOperationException.class);

            verify(venueRepository, never()).delete(any(Venue.class));
        }
    }
}
