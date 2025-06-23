package com.api.worldline.flights.repository;

import com.api.worldline.flights.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("SELECT f FROM Flight f WHERE (:airline IS NULL OR f.airline = :airline) " +
            "AND (:departureAirport IS NULL OR f.departureAirport = :departureAirport) " +
            "AND (:destinationAirport IS NULL OR f.destinationAirport = :destinationAirport) " +
            "AND (:departureTime IS NULL OR f.departureTime >= :departureTime) " +
            "AND (:arrivalTime IS NULL OR f.arrivalTime <= :arrivalTime)")
    List<Flight> searchFlights(@Param("airline") String airline,
                               @Param("departureAirport") String departureAirport,
                               @Param("destinationAirport") String destinationAirport,
                               @Param("departureTime") LocalDateTime departureTime,
                               @Param("arrivalTime") LocalDateTime arrivalTime);
}
