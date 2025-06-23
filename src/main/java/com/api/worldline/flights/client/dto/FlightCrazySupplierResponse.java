package com.api.worldline.flights.client.dto;

import java.time.LocalDateTime;

public record FlightCrazySupplierResponse(String carrier,
                                          double basePrice,
                                          double tax,
                                          String departureAirportName,
                                          String arrivalAirportName,
                                          LocalDateTime outboundDateTime,
                                          LocalDateTime inboundDateTime) {
}
