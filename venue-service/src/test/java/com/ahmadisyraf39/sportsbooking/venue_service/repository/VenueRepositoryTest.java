package com.ahmadisyraf39.sportsbooking.venue_service.repository;

import com.ahmadisyraf39.sportsbooking.venue_service.entity.Venue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class VenueRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private VenueRepository venueRepository;

    @Test
    void shouldSaveAndRetrieveVenue() {
        Venue venue = new Venue();
        venue.setName("Downtown Sports Hall");
        venue.setAddress("123 Main St");
        venue.setDescription("A great venue");
        venue.setOpeningTime(LocalTime.of(8, 0));
        venue.setClosingTime(LocalTime.of(22, 0));
        venue.setOwnerId(1L);

        Venue saved = venueRepository.save(venue);

        assertThat(saved.getId()).isNotNull();
        assertThat(venueRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldFindVenuesByNameContainingIgnoreCase() {
        Venue venue = new Venue();
        venue.setName("Downtown Sports Hall");
        venue.setAddress("123 Main St");
        venue.setOpeningTime(LocalTime.of(8, 0));
        venue.setClosingTime(LocalTime.of(22, 0));
        venue.setOwnerId(1L);
        venueRepository.save(venue);

        List<Venue> found = venueRepository.findByNameContainingIgnoreCase("downtown");

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getName()).isEqualTo("Downtown Sports Hall");
    }

    @Test
    void shouldReturnEmptyWhenNoNameMatches() {
        List<Venue> found = venueRepository.findByNameContainingIgnoreCase("nonexistent");

        assertThat(found).isEmpty();
    }
}
