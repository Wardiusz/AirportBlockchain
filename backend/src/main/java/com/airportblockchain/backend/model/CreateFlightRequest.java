package com.airportblockchain.backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFlightRequest {

    @NotBlank(message = "nie moze byc pusty")
    @Size(min = 2, max = 10, message = "musi miec 2-10 znakow")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "tylko wielkie litery i cyfry (np. LOT101)")
    private String flightId;

    @NotBlank(message = "nie moze byc pusty")
    @Size(max = 100, message = "maksymalnie 100 znakow")
    private String airline;

    @NotBlank(message = "nie moze byc pusty")
    @Pattern(regexp = "^[A-Z]{3}$", message = "kod IATA - 3 wielkie litery (np. WAW)")
    private String origin;

    @NotBlank(message = "nie moze byc pusty")
    @Pattern(regexp = "^[A-Z]{3}$", message = "kod IATA - 3 wielkie litery (np. LHR)")
    private String destination;

    @NotBlank(message = "nie moze byc pusty")
    @Size(max = 10, message = "maksymalnie 10 znakow")
    private String gate;

    @NotBlank(message = "nie moze byc pusty")
    @Pattern(regexp = "^(ON_TIME|DELAYED|CANCELLED|BOARDING|DEPARTED)$",
             message = "dozwolone: ON_TIME, DELAYED, CANCELLED, BOARDING, DEPARTED")
    private String status;

    @NotBlank(message = "nie moze byc pusty")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$",
             message = "format ISO-8601: 2026-05-28T10:00:00Z")
    private String scheduledDep;
}
