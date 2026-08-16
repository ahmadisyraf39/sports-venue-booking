package com.ahmadisyraf39.sportsbooking.venue_service.service;

import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.CourtCreateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.CourtUpdateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.response.CourtResponse;
import com.ahmadisyraf39.sportsbooking.venue_service.entity.Court;
import com.ahmadisyraf39.sportsbooking.venue_service.entity.SportType;
import com.ahmadisyraf39.sportsbooking.venue_service.entity.Venue;
import com.ahmadisyraf39.sportsbooking.venue_service.exception.CourtNotFoundException;
import com.ahmadisyraf39.sportsbooking.venue_service.exception.UnauthorizedOperationException;
import com.ahmadisyraf39.sportsbooking.venue_service.repository.CourtRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private VenueService venueService;

    @InjectMocks
    private CourtService courtService;

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

    private Court existingCourt(Venue venue) {
        Court court = new Court();
        court.setId(1L);
        court.setVenue(venue);
        court.setName("Court 1");
        court.setSportType(SportType.BADMINTON);
        court.setHourlyPrice(new BigDecimal("25.00"));
        return court;
    }

    @Nested
    class AddCourt {

        @Test
        void shouldAddCourt_WhenOwnerIdMatches() {
            Venue venue = existingVenue();
            CourtCreateRequest request = new CourtCreateRequest();
            request.setName("Court 1");
            request.setSportType(SportType.BADMINTON);
            request.setHourlyPrice(new BigDecimal("25.00"));
            request.setOwnerId(10L);

            when(venueService.findVenueById(1L)).thenReturn(venue);
            when(courtRepository.save(any(Court.class))).thenAnswer(invocation -> {
                Court court = invocation.getArgument(0);
                court.setId(1L);
                return court;
            });

            CourtResponse response = courtService.addCourt(1L, request);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getVenueId()).isEqualTo(1L);
        }

        @Test
        void shouldThrowException_WhenOwnerIdDoesNotMatch() {
            Venue venue = existingVenue();
            CourtCreateRequest request = new CourtCreateRequest();
            request.setName("Court 1");
            request.setSportType(SportType.BADMINTON);
            request.setHourlyPrice(new BigDecimal("25.00"));
            request.setOwnerId(999L);

            when(venueService.findVenueById(1L)).thenReturn(venue);

            assertThatThrownBy(() -> courtService.addCourt(1L, request))
                    .isInstanceOf(UnauthorizedOperationException.class);

            verify(courtRepository, never()).save(any(Court.class));
        }
    }

    @Nested
    class UpdateCourt {

        @Test
        void shouldUpdateCourt_WhenOwnerIdMatches() {
            Venue venue = existingVenue();
            Court court = existingCourt(venue);
            CourtUpdateRequest request = new CourtUpdateRequest();
            request.setName("Updated Court");
            request.setSportType(SportType.TENNIS);
            request.setHourlyPrice(new BigDecimal("30.00"));
            request.setOwnerId(10L);

            when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
            when(courtRepository.save(any(Court.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CourtResponse response = courtService.updateCourt(1L, request);

            assertThat(response.getName()).isEqualTo("Updated Court");
        }

        @Test
        void shouldThrowException_WhenOwnerIdDoesNotMatch() {
            Venue venue = existingVenue();
            Court court = existingCourt(venue);
            CourtUpdateRequest request = new CourtUpdateRequest();
            request.setName("Updated Court");
            request.setSportType(SportType.TENNIS);
            request.setHourlyPrice(new BigDecimal("30.00"));
            request.setOwnerId(999L);

            when(courtRepository.findById(1L)).thenReturn(Optional.of(court));

            assertThatThrownBy(() -> courtService.updateCourt(1L, request))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void shouldThrowException_WhenCourtNotFound() {
            CourtUpdateRequest request = new CourtUpdateRequest();
            request.setName("Updated Court");
            request.setSportType(SportType.TENNIS);
            request.setHourlyPrice(new BigDecimal("30.00"));
            request.setOwnerId(10L);

            when(courtRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courtService.updateCourt(99L, request))
                    .isInstanceOf(CourtNotFoundException.class);
        }
    }

    @Nested
    class DeleteCourt {

        @Test
        void shouldDeleteCourt_WhenOwnerIdMatches() {
            Venue venue = existingVenue();
            Court court = existingCourt(venue);

            when(courtRepository.findById(1L)).thenReturn(Optional.of(court));

            courtService.deleteCourt(1L, 10L);

            verify(courtRepository).delete(court);
        }

        @Test
        void shouldThrowException_WhenOwnerIdDoesNotMatch() {
            Venue venue = existingVenue();
            Court court = existingCourt(venue);

            when(courtRepository.findById(1L)).thenReturn(Optional.of(court));

            assertThatThrownBy(() -> courtService.deleteCourt(1L, 999L))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }
    }

    @Nested
    class ListCourtsForVenue {

        @Test
        void shouldReturnCourtsForVenue() {
            Venue venue = existingVenue();
            Court court = existingCourt(venue);

            when(venueService.findVenueById(1L)).thenReturn(venue);
            when(courtRepository.findByVenueId(1L)).thenReturn(List.of(court));

            List<CourtResponse> responses = courtService.listCourtsForVenue(1L);

            assertThat(responses).hasSize(1);
        }
    }
}
