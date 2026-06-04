package com.airportblockchain.backend.service.impl;

import com.airportblockchain.backend.model.FlightData;
import com.airportblockchain.backend.service.FlightService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.client.Contract;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FlightServiceImpl implements FlightService {

    private final Map<String, Contract> roleContracts;
    private final ObjectMapper mapper = new ObjectMapper();

    public FlightServiceImpl(Map<String, Contract> roleContracts) {
        this.roleContracts = roleContracts;
    }

    private Contract contract() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getAuthorities() != null) {
            for (GrantedAuthority ga : auth.getAuthorities()) {
                String authority = ga.getAuthority();
                assert authority != null;
                if (authority.startsWith("ROLE_")) {
                    String role = authority.substring(5);
                    Contract c = roleContracts.get(role);
                    if (c != null) return c;
                }
            }
        }

        Contract fallback = roleContracts.get("ADMIN");
        if (fallback == null) {
            throw new IllegalStateException("Brak dostepnego Contract dla zadnej roli");
        }
        return fallback;
    }

    @Override
    public FlightData getFlight(String flightId) throws Exception {
        byte[] result = contract().evaluateTransaction("queryFlight", flightId);

        return mapper.readValue(result, FlightData.class);
    }

    @Override
    public List<FlightData> getAllFlights() throws Exception {
        byte[] result = contract().evaluateTransaction("queryAllFlights");

        return mapper.readValue(result, new TypeReference<>() {});
    }

    @Override
    public FlightData createFlight(String flightId, String airline, String origin, String destination, String gate, String status, String scheduledDep) throws Exception {
        byte[] result = contract().submitTransaction(
            "createFlight",
            flightId, airline, origin, destination, gate, status, scheduledDep);

        return mapper.readValue(result, FlightData.class);
    }

    @Override
    public FlightData updateStatus(String flightId, String newStatus) throws Exception {
        byte[] result = contract().submitTransaction("updateStatus", flightId, newStatus);

        return mapper.readValue(result, FlightData.class);
    }

    @Override
    public FlightData updateGate(String flightId, String newGate) throws Exception {
        byte[] result = contract().submitTransaction("updateGate", flightId, newGate);

        return mapper.readValue(result, FlightData.class);
    }

    @Override
    public String getFlightHistory(String flightId) throws Exception {
        byte[] result = contract().evaluateTransaction("getFlightHistory", flightId);

        return new String(result);
    }
}
