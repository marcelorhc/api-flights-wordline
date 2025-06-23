package com.api.worldline.flights.controller;

import com.api.worldline.flights.controller.dto.FlightRequest;
import com.api.worldline.flights.controller.dto.FlightResponse;
import com.api.worldline.flights.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    @Operation(summary = "Get all flights, from database and suppliers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of flights")
    })
    public List<FlightResponse> getFlights(
            @Parameter(description = "Airline name")
            @RequestParam(required = false) String airline,

            @Parameter(description = "3-letter departure airport code")
            @RequestParam(required = false)
            @Size(min = 3, max = 3, message = "Departure airport code must be exactly 3 characters")

            String departureAirport,
            @Parameter(description = "3-letter destination airport code")
            @RequestParam(required = false)
            @Size(min = 3, max = 3, message = "Destination airport code must be exactly 3 characters")

            String destinationAirport,
            @Parameter(description = "Departure time in ISO-8601 format")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureTime,

            @Parameter(description = "Arrival time in ISO-8601 format")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime arrivalTime) {
        return flightService.getFlights(airline, departureAirport, destinationAirport, departureTime, arrivalTime);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Create a new flight information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a flight")
    })
    public FlightResponse saveFlightInfo(@RequestBody @Valid FlightRequest flightRequest) {
        return flightService.saveFlightInfo(flightRequest);
    }

    @PutMapping
    @Operation(summary = "Update an existing flight information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated a flight")
    })
    public void updateFlightInfo(@Parameter(description = "Unique identifier of the flight from database") @RequestParam Long id, @RequestBody @Valid FlightRequest flightRequest) {
        flightService.updateFlightInfo(id, flightRequest);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    @Operation(summary = "Delete an existing flight information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted a flight")
    })
    public void deleteFlightInfo(Long id) {
        flightService.deleteFlightInfo(id);
    }

}
