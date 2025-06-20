package com.api.wordline.flights.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CustomFeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());

        return switch (status.series()) {
            case CLIENT_ERROR ->
                    new ResponseStatusException(status, "Client error occurred when calling: " + methodKey);
            case SERVER_ERROR ->
                    new ResponseStatusException(status, "Server error occurred when calling: " + methodKey);
            default -> defaultDecoder.decode(methodKey, response);
        };
    }
}
