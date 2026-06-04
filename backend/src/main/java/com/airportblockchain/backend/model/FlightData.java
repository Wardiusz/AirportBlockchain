package com.airportblockchain.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON from Ledger without conversion
 */
public record FlightData(@JsonProperty("flightId") String flightId, @JsonProperty("airline") String airline,
                         @JsonProperty("origin") String origin, @JsonProperty("destination") String destination,
                         @JsonProperty("gate") String gate, @JsonProperty("status") String status,
                         @JsonProperty("scheduledDep") String scheduledDep,
                         @JsonProperty("lastUpdated") String lastUpdated, @JsonProperty("updatedBy") String updatedBy) {

    @JsonCreator
    public FlightData {}
}