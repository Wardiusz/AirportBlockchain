package com.airportblockchain.backend.service.impl;

import com.airportblockchain.backend.model.FlightData;
import com.airportblockchain.backend.service.FlightService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Network;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlightServiceImpl implements FlightService {

    private final Contract contract;
    private final ObjectMapper mapper = new ObjectMapper();

    public FlightServiceImpl(
            Gateway gateway,
            @Value("${fabric.channel}") String channelName,
            @Value("${fabric.chaincode}") String chaincodeName) {
        Network network = gateway.getNetwork(channelName);
        this.contract = network.getContract(chaincodeName);
    }

    // ── Odczyt pojedynczego lotu ───────────────────────────────
    @Override
    public FlightData getFlight(String flightId) throws Exception {
        byte[] result = contract.evaluateTransaction("queryFlight", flightId);

        return mapper.readValue(result, FlightData.class);
    }

    // ── Odczyt wszystkich lotów ────────────────────────────────
    @Override
    public List<FlightData> getAllFlights() throws Exception {
        byte[] result = contract.evaluateTransaction("queryAllFlights");

        return mapper.readValue(result, new TypeReference<>(){});
    }

    // ── Tworzenie lotu ─────────────────────────────────────────
    @Override
    public FlightData createFlight(String flightId, String airline, String origin, String destination,
                                   String gate, String status, String scheduledDep) throws Exception {
        byte[] result = contract.submitTransaction(
                "createFlight",
                flightId, airline, origin, destination,
                gate, status, scheduledDep
        );

        return mapper.readValue(result, FlightData.class);
    }

    // ── Aktualizacja statusu ───────────────────────────────────
    @Override
    public FlightData updateStatus(String flightId, String newStatus) throws Exception {
        byte[] result = contract.submitTransaction("updateStatus", flightId, newStatus);

        return mapper.readValue(result, FlightData.class);
    }

    // ── Aktualizacja bramki ────────────────────────────────────
    @Override
    public FlightData updateGate(String flightId, String newGate) throws Exception {
        byte[] result = contract.submitTransaction("updateGate", flightId, newGate);

        return mapper.readValue(result, FlightData.class);
    }

    // ── Historia zmian (tylko ADMIN) ───────────────────────────
    @Override
    public String getFlightHistory(String flightId) throws Exception {
        byte[] result = contract.evaluateTransaction("getFlightHistory", flightId);

        return new String(result);
    }
}
