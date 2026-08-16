package com.ahmadisyraf39.sportsbooking.venue_service.repository;

import com.ahmadisyraf39.sportsbooking.venue_service.entity.Court;
import com.ahmadisyraf39.sportsbooking.venue_service.entity.SportType;
import com.ahmadisyraf39.sportsbooking.venue_service.entity.Venue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class CourtRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private CourtRepository courtRepository;

    private Venue persistVenue() {
        Venue venue = new Venue();
        venue.setName("Downtown Sports Hall");
        venue.setAddress("123 Main St");
        venue.setOpeningTime(LocalTime.of(8, 0));
        venue.setClosingTime(LocalTime.of(22, 0));
        venue.setOwnerId(1L);
        return venueRepository.save(venue);
    }

    @Test
    void shouldSaveAndRetrieveCourtsByVenueId() {
        Venue venue = persistVenue();

        Court court = new Court();
        court.setVenue(venue);
        court.setName("Court 1");
        court.setSportType(SportType.BADMINTON);
        court.setHourlyPrice(new BigDecimal("25.00"));
        courtRepository.save(court);

        List<Court> found = courtRepository.findByVenueId(venue.getId());

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getName()).isEqualTo("Court 1");
    }

    @Test
    void shouldReturnEmptyWhenVenueHasNoCourts() {
        Venue venue = persistVenue();

        List<Court> found = courtRepository.findByVenueId(venue.getId());

        assertThat(found).isEmpty();
    }
}
