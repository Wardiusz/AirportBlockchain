package com.airportblockchain.backend.service;

import com.airportblockchain.backend.model.FlightData;
import com.airportblockchain.backend.service.impl.FlightServiceImpl;
import org.hyperledger.fabric.client.Contract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlightServiceImpl — testy jednostkowe (mock Fabric)")
class FlightServiceImplTest {

    @Mock
    private Contract contract;

    private FlightServiceImpl service;

    @BeforeEach
    void setUp() {
        // Konfigurujemy mapę kontraktów tak, aby zawierała domyślny fallback dla "ADMIN"
        Map<String, Contract> roleContracts = Map.of("ADMIN", contract);

        // Wstrzykujemy mapę do zaktualizowanego konstruktora
        service = new FlightServiceImpl(roleContracts);
    }

    private byte[] json(String flightId, String status, String gate) {
        return ("""
            {
              "flightId": "%s",
              "airline": "LOT Polish Airlines",
              "origin": "WAW",
              "destination": "JFK",
              "gate": "%s",
              "status": "%s",
              "scheduledDep": "2026-05-29T18:00:00Z",
              "lastUpdated": "2026-05-29T17:00:00Z",
              "updatedBy": "ADMIN"
            }
            """.formatted(flightId, gate, status)).getBytes(StandardCharsets.UTF_8);
    }

    // ── DODAWANIE REKORDU ─────────────────────────────────────

    @Test
    @DisplayName("createFlight wysyła transakcję 'createFlight' i zwraca lot")
    void createFlight_submitsTransactionAndReturnsFlight() throws Exception {
        when(contract.submitTransaction(
                "createFlight", "LO100", "LOT Polish Airlines",
                "WAW", "JFK", "D5", "ON_TIME", "2026-05-29T18:00:00Z"))
                .thenReturn(json("LO100", "ON_TIME", "D5"));

        FlightData result = service.createFlight(
                "LO100", "LOT Polish Airlines",
                "WAW", "JFK", "D5", "ON_TIME", "2026-05-29T18:00:00Z");

        // Sprawdź że odpowiedź sparsowała się poprawnie
        assertThat(result.flightId()).isEqualTo("LO100");
        assertThat(result.status()).isEqualTo("ON_TIME");
        assertThat(result.gate()).isEqualTo("D5");

        // Sprawdź że transakcja została wysłana dokładnie raz
        verify(contract, times(1)).submitTransaction(
                "createFlight", "LO100", "LOT Polish Airlines",
                "WAW", "JFK", "D5", "ON_TIME", "2026-05-29T18:00:00Z");
    }

    // ── MODYFIKACJA STATUSU ───────────────────────────────────

    @Test
    @DisplayName("updateStatus zmienia status lotu na DELAYED")
    void updateStatus_changesStatus() throws Exception {
        when(contract.submitTransaction("updateStatus", "LO100", "DELAYED"))
                .thenReturn(json("LO100", "DELAYED", "D5"));

        FlightData result = service.updateStatus("LO100", "DELAYED");

        assertThat(result.status()).isEqualTo("DELAYED");
        verify(contract).submitTransaction("updateStatus", "LO100", "DELAYED");
    }

    // ── MODYFIKACJA BRAMKI ────────────────────────────────────

    @Test
    @DisplayName("updateGate zmienia bramkę lotu")
    void updateGate_changesGate() throws Exception {
        when(contract.submitTransaction("updateGate", "LO100", "C12"))
                .thenReturn(json("LO100", "ON_TIME", "C12"));

        FlightData result = service.updateGate("LO100", "C12");

        assertThat(result.gate()).isEqualTo("C12");
        verify(contract).submitTransaction("updateGate", "LO100", "C12");
    }

    // ── ODCZYT POJEDYNCZEGO LOTU ──────────────────────────────

    @Test
    @DisplayName("getFlight pobiera lot przez evaluateTransaction")
    void getFlight_returnsFlight() throws Exception {
        when(contract.evaluateTransaction("queryFlight", "LO100"))
                .thenReturn(json("LO100", "ON_TIME", "D5"));

        FlightData result = service.getFlight("LO100");

        assertThat(result.flightId()).isEqualTo("LO100");
        verify(contract).evaluateTransaction("queryFlight", "LO100");
    }

    // ── ODCZYT WSZYSTKICH LOTÓW ───────────────────────────────

    @Test
    @DisplayName("getAllFlights parsuje listę lotów")
    void getAllFlights_returnsList() throws Exception {
        byte[] arrayJson = ("[" +
                new String(json("LO100", "ON_TIME", "D5")) + "," +
                new String(json("LO200", "DELAYED", "A3")) +
                "]").getBytes(StandardCharsets.UTF_8);

        when(contract.evaluateTransaction("queryAllFlights")).thenReturn(arrayJson);

        List<FlightData> result = service.getAllFlights();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).flightId()).isEqualTo("LO100");
        assertThat(result.get(1).status()).isEqualTo("DELAYED");
    }
}