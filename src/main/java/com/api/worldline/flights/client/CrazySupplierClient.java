package com.api.worldline.flights.client;

import com.api.worldline.flights.client.dto.FlightCrazySupplierRequest;
import com.api.worldline.flights.client.dto.FlightCrazySupplierResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "api-client", url = "${feign.crazy-suppliers-url}")
public interface CrazySupplierClient {

    @PostMapping("/flights")
    List<FlightCrazySupplierResponse> getFlights(@RequestBody FlightCrazySupplierRequest user);

}
