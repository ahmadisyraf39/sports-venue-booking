package com.ahmadisyraf39.sportsbooking.venue_service.repository;

import com.ahmadisyraf39.sportsbooking.venue_service.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {

    List<Court> findByVenueId(Long venueId);

}
