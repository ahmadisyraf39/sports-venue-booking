package com.ahmadisyraf39.sportsbooking.venue_service.repository;

import com.ahmadisyraf39.sportsbooking.venue_service.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByNameContainingIgnoreCase(String name);

}
