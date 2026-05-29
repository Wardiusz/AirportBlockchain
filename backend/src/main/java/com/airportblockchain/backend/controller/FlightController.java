package com.airportblockchain.backend.controller;

import com.airportblockchain.backend.model.FlightData;
import com.airportblockchain.backend.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * FlightController — REST API dla danych lotów.
 *
 * GET    /api/flights          → wszystkie loty
 * GET    /api/flights/{id}     → pojedynczy lot
 * POST   /api/flights          → nowy lot
 * PATCH  /api/flights/{id}/status → zmiana statusu
 * PATCH  /api/flights/{id}/gate   → zmiana bramki
 * GET    /api/flights/{id}/history → historia zmian
 */
@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    public ResponseEntity<?> getAllFlights() {
        try {
            return ResponseEntity.ok(flightService.getAllFlights());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage(), "cause",
                            e.getCause() != null ? e.getCause().getMessage() : "brak"));
        }
    }

    @GetMapping("/{flightId}")
    public ResponseEntity<?> getFlight(@PathVariable String flightId) {
        try {
            return ResponseEntity.ok(flightService.getFlight(flightId));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createFlight(@RequestBody Map<String, String> body) {
        try {
            FlightData created = flightService.createFlight(
                    body.get("flightId"), body.get("airline"),
                    body.get("origin"),   body.get("destination"),
                    body.get("gate"),     body.get("status"),
                    body.get("scheduledDep")
            );
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{flightId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String flightId,
            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(
                    flightService.updateStatus(flightId, body.get("status")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{flightId}/gate")
    public ResponseEntity<?> updateGate(
            @PathVariable String flightId,
            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(
                    flightService.updateGate(flightId, body.get("gate")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{flightId}/history")
    public ResponseEntity<?> getFlightHistory(@PathVariable String flightId) {
        try {
            return ResponseEntity.ok(flightService.getFlightHistory(flightId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
