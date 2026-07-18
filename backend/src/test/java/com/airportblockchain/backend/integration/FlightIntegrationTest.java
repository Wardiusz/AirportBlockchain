package com.airportblockchain.backend.integration;

import com.airportblockchain.backend.model.FlightData;
import com.airportblockchain.backend.service.FlightService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test integracyjny — łączy się z siecią Hyperledger Fabric
 * i zapisuje oraz modyfikuje rekord w ledgerze.
 *
 * WYMAGA uruchomionej sieci (start.sh) oraz poprawnego .env
 *
 * Uruchamia się tylko gdy ustawiona jest zmienna środowiskowa:
 *   RUN_INTEGRATION=true
 *
 * Przykład uruchomienia:
 *   RUN_INTEGRATION=true mvn test -Dtest=FlightIntegrationTest
 *
 * Bez tej zmiennej test jest pomijany — dzięki temu zwykły `mvn test`
 * (np. na CI bez blockchaina) nie wywala builda.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_INTEGRATION", matches = "true")
@DisplayName("Integracja — zapis i modyfikacja na zywym blockchainie")
class FlightIntegrationTest {

    @Autowired
    private FlightService flightService;

    @Test
    @DisplayName("Dodaje lot, odczytuje go, a potem zmienia status i bramke")
    void createReadAndModifyFlight() throws Exception {
        // Unikalne ID żeby test można było uruchamiać wielokrotnie
        String flightId = "IT" + (System.currentTimeMillis() % 1_000_000);

        //  1. Dodaj lot do blockchaina
        FlightData created = flightService.createFlight(
            flightId, "Integration Air",
            "WAW", "JFK", "D1", "ON_TIME", "2026-05-29T18:00:00Z");

        assertThat(created.flightId()).isEqualTo(flightId);
        assertThat(created.status()).isEqualTo("ON_TIME");

        //  2. Odczytaj lot z blockchaina
        FlightData fetched = flightService.getFlight(flightId);
        assertThat(fetched.flightId()).isEqualTo(flightId);
        assertThat(fetched.gate()).isEqualTo("D1");

        //  3. Zmień status
        FlightData delayed = flightService.updateStatus(flightId, "DELAYED");
        assertThat(delayed.status()).isEqualTo("DELAYED");

        //  4. Zmień bramkę
        FlightData regated = flightService.updateGate(flightId, "C7");
        assertThat(regated.gate()).isEqualTo("C7");
        // Status z poprzedniej zmiany powinien się utrzymać
        assertThat(regated.status()).isEqualTo("DELAYED");

        //  5. Potwierdź że zmiany są trwałe w ledgerze 
        FlightData finalState = flightService.getFlight(flightId);
        assertThat(finalState.status()).isEqualTo("DELAYED");
        assertThat(finalState.gate()).isEqualTo("C7");
    }
}
