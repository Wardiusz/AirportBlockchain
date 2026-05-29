package com.airportblockchain.backend.service;

import com.airportblockchain.backend.model.FlightData;

import java.util.List;

public interface FlightService {
    FlightData getFlight(String flightId) throws Exception;
    List<FlightData> getAllFlights() throws Exception;
    FlightData updateGate(String flightId, String newGate) throws Exception;
    FlightData createFlight(String flightId, String airline, String origin, String destination, String gate, String status, String scheduledDep) throws Exception;
    FlightData updateStatus(String flightId, String newStatus) throws Exception;
    String getFlightHistory(String flightId) throws Exception;
}