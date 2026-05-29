package com.airportblockchain.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * FlightData — model danych lotu.
 *
 * JSON z ledgera bez żadnej konwersji.
 */
public final class FlightData {

    @JsonProperty("flightId")
    private final String flightId;

    @JsonProperty("airline")
    private final String airline;

    @JsonProperty("origin")
    private final String origin;

    @JsonProperty("destination")
    private final String destination;

    @JsonProperty("gate")
    private final String gate;

    @JsonProperty("status")
    private final String status;

    @JsonProperty("scheduledDep")
    private final String scheduledDep;

    @JsonProperty("lastUpdated")
    private final String lastUpdated;

    @JsonProperty("updatedBy")
    private final String updatedBy;

    @JsonCreator
    public FlightData(
            @JsonProperty("flightId")     String flightId,
            @JsonProperty("airline")      String airline,
            @JsonProperty("origin")       String origin,
            @JsonProperty("destination")  String destination,
            @JsonProperty("gate")         String gate,
            @JsonProperty("status")       String status,
            @JsonProperty("scheduledDep") String scheduledDep,
            @JsonProperty("lastUpdated")  String lastUpdated,
            @JsonProperty("updatedBy")    String updatedBy) {
        this.flightId    = flightId;
        this.airline     = airline;
        this.origin      = origin;
        this.destination = destination;
        this.gate        = gate;
        this.status      = status;
        this.scheduledDep = scheduledDep;
        this.lastUpdated = lastUpdated;
        this.updatedBy   = updatedBy;
    }

    public String getFlightId()     { return flightId; }
    public String getAirline()      { return airline; }
    public String getOrigin()       { return origin; }
    public String getDestination()  { return destination; }
    public String getGate()         { return gate; }
    public String getStatus()       { return status; }
    public String getScheduledDep() { return scheduledDep; }
    public String getLastUpdated()  { return lastUpdated; }
    public String getUpdatedBy()    { return updatedBy; }
}