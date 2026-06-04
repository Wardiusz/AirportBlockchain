package com.airportblockchain.backend.controller;

import com.airportblockchain.backend.model.CreateFlightRequest;
import com.airportblockchain.backend.model.UpdateGateRequest;
import com.airportblockchain.backend.model.UpdateStatusRequest;
import com.airportblockchain.backend.service.FlightService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * GET    /api/flights              → AIRLINE, HANDLER, ADMIN
 * GET    /api/flights/{id}         → AIRLINE, HANDLER, ADMIN
 * POST   /api/flights              → AIRLINE, ADMIN
 * PATCH  /api/flights/{id}/status  → AIRLINE, ADMIN
 * PATCH  /api/flights/{id}/gate    → AIRLINE, HANDLER, ADMIN
 * GET    /api/flights/{id}/history → ADMIN
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllFlights() throws Exception {
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    @GetMapping("/{flightId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFlight(@PathVariable String flightId) throws Exception {
        return ResponseEntity.ok(flightService.getFlight(flightId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AIRLINE', 'ADMIN')")
    public ResponseEntity<?> createFlight(@Valid @RequestBody CreateFlightRequest req) throws Exception {
        return ResponseEntity.ok(flightService.createFlight(
            req.getFlightId(), req.getAirline(),
            req.getOrigin(),   req.getDestination(),
            req.getGate(),     req.getStatus(),
            req.getScheduledDep()
        ));
    }

    @PatchMapping("/{flightId}/status")
    @PreAuthorize("hasAnyRole('AIRLINE', 'ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable String flightId, @Valid @RequestBody UpdateStatusRequest req) throws Exception {
        return ResponseEntity.ok(flightService.updateStatus(flightId, req.getStatus()));
    }

    @PatchMapping("/{flightId}/gate")
    @PreAuthorize("hasAnyRole('AIRLINE', 'HANDLER', 'ADMIN')")
    public ResponseEntity<?> updateGate(@PathVariable String flightId, @Valid @RequestBody UpdateGateRequest req) throws Exception {
        return ResponseEntity.ok(flightService.updateGate(flightId, req.getGate()));
    }

    @GetMapping("/{flightId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getFlightHistory(@PathVariable String flightId) throws Exception {
        return ResponseEntity.ok(flightService.getFlightHistory(flightId));
    }
}
