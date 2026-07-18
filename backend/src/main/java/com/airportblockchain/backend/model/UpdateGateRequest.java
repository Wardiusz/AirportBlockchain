package com.airportblockchain.backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO dla PATCH /api/flights/{id}/gate
 * */
@Setter
@Getter
public class UpdateGateRequest {

    @NotBlank(message = "nie moze byc pusty")
    @Size(min = 1, max = 10, message = "musi miec 1-10 znakow")
    private String gate;

}
