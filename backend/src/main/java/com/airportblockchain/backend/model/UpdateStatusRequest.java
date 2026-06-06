package com.airportblockchain.backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/** DTO dla PATCH /api/flights/{id}/status */
@Setter
@Getter
public class UpdateStatusRequest {

    @NotBlank(message = "nie może być pusty")
    @Pattern(regexp = "^(ON_TIME|DELAYED|CANCELLED|BOARDING|DEPARTED)$",
             message = "dozwolone: ON_TIME, DELAYED, CANCELLED, BOARDING, DEPARTED")
    private String status;

}
