package com.api.wordline.flights.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ErrorResponse(
        @Schema(description = "List of error messages", example = "[Error message 1, Error message 2]")
        List<String> messages
) {
}
