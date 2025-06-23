package com.api.worldline.flights.client.dto;

import java.time.LocalDateTime;

public record FlightCrazySupplierRequest(String from,
                                         String to,
                                         LocalDateTime outboundDate,
                                         LocalDateTime inboundDate) {
}
