package com.api.wordline.flights.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record FlightResponse(
        @Schema(description = "Unique identifier of the flight from database", example = "123")
        Long id,

        @Schema(description = "Airline name", example = "Delta")
        String airline,

        @Schema(description = "Flight supplier", example = "Internal")
        String supplier,

        @Schema(description = "Fare price of the flight", example = "299.99")
        Double fare,

        @Schema(description = "3-letter departure airport code", example = "GRU", minLength = 3, maxLength = 3)
        @Size(min = 3, max = 3, message = "Departure airport code must be exactly 3 characters")
        String departureAirport,

        @Schema(description = "3-letter destination airport code", example = "JFK", minLength = 3, maxLength = 3)
        @Size(min = 3, max = 3, message = "Destination airport must be exactly 3 characters")
        String destinationAirport,

        @Schema(description = "Departure time in ISO-8601 format", example = "2025-07-01T10:00:00")
        LocalDateTime departureTime,

        @Schema(description = "Arrival time in ISO-8601 format", example = "2025-07-01T18:00:00")
        LocalDateTime arrivalTime
) {
}
