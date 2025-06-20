package com.api.wordline.flights.service;

import com.api.wordline.flights.client.CrazySupplierClient;
import com.api.wordline.flights.client.dto.FlightCrazySupplierRequest;
import com.api.wordline.flights.client.dto.FlightCrazySupplierResponse;
import com.api.wordline.flights.config.FlightMapper;
import com.api.wordline.flights.controller.dto.FlightRequest;
import com.api.wordline.flights.controller.dto.FlightResponse;
import com.api.wordline.flights.exception.BusinessException;
import com.api.wordline.flights.exception.TechnicalException;
import com.api.wordline.flights.model.Flight;
import com.api.wordline.flights.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightServiceTest {

    @Mock
    private FlightMapper flightMapper;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private CrazySupplierClient crazySupplierClient;

    @InjectMocks
    private FlightService flightService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getFlightsShouldReturnCombinedFlights() {
        String airline = "AirX";
        String dep = "JFK";
        String dest = "LAX";
        LocalDateTime depTime = LocalDateTime.now();
        LocalDateTime arrTime = depTime.plusHours(5);

        Flight flight = new Flight();
        FlightRequest flightRequest = getFlightDTO();
        FlightCrazySupplierResponse supplierResponse = new FlightCrazySupplierResponse(
                "LATAM",
                800.0,
                70.0,
                "GRU",
                "JFK",
                depTime,
                arrTime
        );
        FlightResponse supplierDTO = getFlightResponse();

        when(flightRepository.searchFlights(airline, dep, dest, depTime, arrTime)).thenReturn(List.of(flight));

        when(crazySupplierClient.getFlights(any(FlightCrazySupplierRequest.class))).thenReturn(List.of(supplierResponse));
        when(flightMapper.fromCrazySupplierToResponse(supplierResponse)).thenReturn(supplierDTO);

        List<FlightResponse> result = flightService.getFlights(airline, dep, dest, depTime, arrTime);

        assertEquals(2, result.size());
        verify(flightRepository).searchFlights(airline, dep, dest, depTime, arrTime);
        verify(crazySupplierClient).getFlights(any());
    }

    @Test
    void getFlightsShouldHandleCrazySupplierException() {
        when(flightRepository.searchFlights(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(crazySupplierClient.getFlights(any())).thenThrow(new RuntimeException("Supplier error"));

        List<FlightResponse> result = flightService.getFlights("A", "B", "C", LocalDateTime.now(), LocalDateTime.now().plusHours(2));

        assertTrue(result.isEmpty());
    }

    @Test
    void saveFlightInfoShouldReturnSavedDTO() {
        FlightRequest dto = getFlightDTO();
        Flight entity = new Flight();
        Flight savedEntity = new Flight();

        when(flightMapper.toEntity(dto)).thenReturn(entity);
        when(flightRepository.save(entity)).thenReturn(savedEntity);

        flightService.saveFlightInfo(dto);

        verify(flightRepository).save(entity);
    }

    @Test
    void saveFlightInfoShouldThrowTechnicalException() {
        when(flightMapper.toEntity(any())).thenThrow(new RuntimeException("Mapping failed"));

        assertThrows(TechnicalException.class, () -> flightService.saveFlightInfo(getFlightDTO()));
    }

    @Test
    void updateFlightInfoShouldUpdateSuccessfully() {
        Long id = 1L;
        Flight existing = new Flight();
        existing.setId(id);

        FlightRequest dto = getFlightDTO();
        Flight updated = new Flight();

        when(flightRepository.findById(id)).thenReturn(Optional.of(existing));
        when(flightMapper.toEntity(dto)).thenReturn(updated);

        flightService.updateFlightInfo(id, dto);

        assertEquals(id, updated.getId());
        verify(flightRepository).save(updated);
    }

    @Test
    void updateFlightInfoShouldThrowBusinessExceptionIfNotFound() {
        when(flightRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> flightService.updateFlightInfo(1L, getFlightDTO()));
    }

    @Test
    void updateFlightInfoShouldThrowTechnicalExceptionOnError() {
        Long id = 1L;
        Flight existing = new Flight();
        existing.setId(id);

        when(flightRepository.findById(id)).thenReturn(Optional.of(existing));
        when(flightMapper.toEntity(any())).thenThrow(new RuntimeException());

        assertThrows(TechnicalException.class, () -> flightService.updateFlightInfo(id, getFlightDTO()));
    }

    @Test
    void deleteFlightInfoShouldCallRepository() {
        Long id = 1L;
        flightService.deleteFlightInfo(id);
        verify(flightRepository).deleteById(id);
    }

    @Test
    void deleteFlightInfoShouldThrowTechnicalExceptionOnError() {
        doThrow(new RuntimeException()).when(flightRepository).deleteById(anyLong());
        assertThrows(TechnicalException.class, () -> flightService.deleteFlightInfo(1L));
    }

    private FlightRequest getFlightDTO() {
        LocalDateTime depTime = LocalDateTime.of(2025, 7, 1, 10, 0);
        LocalDateTime arrTime = LocalDateTime.of(2025, 7, 1, 18, 0);

        return new FlightRequest(
                "LATAM",
                "Internal",
                999.99,
                "GRU",
                "JFK",
                depTime,
                arrTime
        );

    }
    private FlightResponse getFlightResponse() {
        LocalDateTime depTime = LocalDateTime.of(2025, 7, 1, 10, 0);
        LocalDateTime arrTime = LocalDateTime.of(2025, 7, 1, 18, 0);

        return new FlightResponse(
                1L,
                "LATAM",
                "Internal",
                999.99,
                "GRU",
                "JFK",
                depTime,
                arrTime
        );
    }
}
