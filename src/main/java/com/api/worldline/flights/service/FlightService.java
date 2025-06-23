package com.api.worldline.flights.service;

import com.api.worldline.flights.client.CrazySupplierClient;
import com.api.worldline.flights.client.dto.FlightCrazySupplierRequest;
import com.api.worldline.flights.client.dto.FlightCrazySupplierResponse;
import com.api.worldline.flights.config.FlightMapper;
import com.api.worldline.flights.controller.dto.FlightRequest;
import com.api.worldline.flights.controller.dto.FlightResponse;
import com.api.worldline.flights.exception.BusinessException;
import com.api.worldline.flights.exception.TechnicalException;
import com.api.worldline.flights.model.Flight;
import com.api.worldline.flights.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightMapper flightMapper;

    private final FlightRepository flightRepository;
    private final CrazySupplierClient crazySupplierClient;

    public List<FlightResponse> getFlights(String airline,
                                          String departureAirport,
                                          String destinationAirport,
                                          LocalDateTime departureTime,
                                          LocalDateTime arrivalTime) {

        List<FlightResponse> result = new ArrayList<>();
        List<Flight> flightInfos = flightRepository.searchFlights(airline, departureAirport, destinationAirport, departureTime, arrivalTime);
        if (!CollectionUtils.isEmpty(flightInfos)) {
            result.addAll(
                    flightInfos.stream()
                            .map(flightMapper::toResponse)
                            .toList()
            );
        }

        try {
            List<FlightCrazySupplierResponse> flights = crazySupplierClient.getFlights(new FlightCrazySupplierRequest(departureAirport, destinationAirport, departureTime, arrivalTime));
            if (!CollectionUtils.isEmpty(flights)) {
                result.addAll(
                        flights.stream()
                                .map(flightMapper::fromCrazySupplierToResponse)
                                .toList()
                );
            }
        } catch (Exception ex) {
            log.error("Error on calling Crazy supplier", ex);
        }

        return result;
    }

    public FlightResponse saveFlightInfo(FlightRequest flightRequest) {
        try {
            Flight flight = flightMapper.toEntity(flightRequest);
            Flight savedFlight = flightRepository.save(flight);
            return flightMapper.toResponse(savedFlight);
        } catch (Exception e) {
            log.error("Error on saving flight", e);
            throw new TechnicalException("Error on saving flight");
        }
    }

    public void updateFlightInfo(Long id, FlightRequest flightRequest) {
        Flight flight = flightRepository.findById(id).orElseThrow(() -> new BusinessException("Flight doenst exist"));
        try {
            Flight flightUpdated = flightMapper.toEntity(flightRequest);
            flightUpdated.setId(flight.getId());
            flightRepository.save(flightUpdated);
        } catch (Exception e) {
            throw new TechnicalException("Error on updating flight");
        }
    }

    public void deleteFlightInfo(Long id) {
        try {
            flightRepository.deleteById(id);
        } catch (Exception e) {
            throw new TechnicalException("Error on deleting flight");
        }
    }

}
