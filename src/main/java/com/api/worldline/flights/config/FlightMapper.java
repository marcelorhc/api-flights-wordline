package com.api.worldline.flights.config;

import com.api.worldline.flights.client.dto.FlightCrazySupplierResponse;
import com.api.worldline.flights.controller.dto.FlightRequest;
import com.api.worldline.flights.controller.dto.FlightResponse;
import com.api.worldline.flights.model.Flight;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FlightMapper {
    FlightResponse toResponse(Flight flight);

    Flight toEntity(FlightRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fare", expression = "java(dto.basePrice() + dto.tax())")
    @Mapping(source = "carrier", target = "airline")
    @Mapping(target = "supplier", constant = "CrazySupplier")
    @Mapping(source = "departureAirportName", target = "departureAirport")
    @Mapping(source = "arrivalAirportName", target = "destinationAirport")
    @Mapping(source = "outboundDateTime", target = "departureTime")
    @Mapping(source = "inboundDateTime", target = "arrivalTime")
    FlightResponse fromCrazySupplierToResponse(FlightCrazySupplierResponse dto);

}
