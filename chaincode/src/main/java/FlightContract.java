import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.annotation.*;


/**
 * FlightContract — wszystkie metody zwracają String (JSON),
 * bo Fabric shim serializuje zwracane obiekty Gensonem (nie Jacksonem).
 * Spring Boot deserializuje String → obiekt po swojej stronie.
 */
@Contract(name = "FlightContract",
        info = @Info(title = "Airport Flight Contract",
                description = "System bezpiecznego udostepniania danych lotow",
                version = "1.0.0"))
@Default
public final class FlightContract implements ContractInterface {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final List<String> VALID_STATUSES = List.of(
            "ON_TIME", "DELAYED", "CANCELLED", "BOARDING", "DEPARTED"
    );

    // ── Inicjalizacja ──────────────────────────────────────────

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        createFlight(ctx, "LOT101", "LOT Polish Airlines", "WAW", "LHR", "B12", "ON_TIME", "2026-05-28T10:00:00Z");
        createFlight(ctx, "FR1234", "Ryanair",             "WAW", "DUB", "A3",  "ON_TIME", "2026-05-28T12:30:00Z");
        createFlight(ctx, "LH5678", "Lufthansa",           "WAW", "FRA", "C7",  "DELAYED", "2026-05-28T14:00:00Z");
    }

    // ── Tworzenie lotu → zwraca String (JSON) ─────────────────

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String createFlight(final Context ctx,
                               final String flightId,
                               final String airline,
                               final String origin,
                               final String destination,
                               final String gate,
                               final String status,
                               final String scheduledDep) {
        String callerRole = getCallerRole(ctx);
        if (!callerRole.equals("AIRLINE") && !callerRole.equals("ADMIN")) {
            throw new ChaincodeException("Brak uprawnien: tylko AIRLINE lub ADMIN moze tworzyc loty", "UNAUTHORIZED");
        }

        String existing = ctx.getStub().getStringState(flightId);
        if (existing != null && !existing.isEmpty()) {
            throw new ChaincodeException("Lot " + flightId + " juz istnieje", "FLIGHT_ALREADY_EXISTS");
        }

        validateStatus(status);

        String txTimestamp = ctx.getStub().getTxTimestamp().toString();

        FlightData flight = new FlightData(
                flightId, airline, origin, destination,
                gate, status, scheduledDep,
                txTimestamp, callerRole
        );

        String json = serialize(flight);
        ctx.getStub().putStringState(flightId, json);
        ctx.getStub().setEvent("FlightCreated", json.getBytes());
        return json;
    }

    // ── Aktualizacja statusu → zwraca String ──────────────────

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String updateStatus(final Context ctx,
                               final String flightId,
                               final String newStatus) {
        String callerRole = getCallerRole(ctx);
        if (!callerRole.equals("AIRLINE") && !callerRole.equals("ADMIN")) {
            throw new ChaincodeException("Brak uprawnien", "UNAUTHORIZED");
        }

        validateStatus(newStatus);
        FlightData existing = getFlightOrThrow(ctx, flightId);

        String txTimestamp = ctx.getStub().getTxTimestamp().toString();

        FlightData updated = new FlightData(
                existing.getFlightId(), existing.getAirline(),
                existing.getOrigin(),   existing.getDestination(),
                existing.getGate(),     newStatus,
                existing.getScheduledDep(),
                txTimestamp, callerRole
        );

        String json = serialize(updated);
        ctx.getStub().putStringState(flightId, json);
        ctx.getStub().setEvent("StatusUpdated", json.getBytes());
        return json;
    }

    // ── Aktualizacja bramki → zwraca String ───────────────────

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String updateGate(final Context ctx,
                             final String flightId,
                             final String newGate) {
        String callerRole = getCallerRole(ctx);
        if (!callerRole.equals("AIRLINE") && !callerRole.equals("HANDLER") && !callerRole.equals("ADMIN")) {
            throw new ChaincodeException("Brak uprawnien", "UNAUTHORIZED");
        }

        FlightData existing = getFlightOrThrow(ctx, flightId);

        String txTimestamp = ctx.getStub().getTxTimestamp().toString();

        FlightData updated = new FlightData(
                existing.getFlightId(), existing.getAirline(),
                existing.getOrigin(),   existing.getDestination(),
                newGate,                existing.getStatus(),
                existing.getScheduledDep(),
                txTimestamp, callerRole
        );

        String json = serialize(updated);
        ctx.getStub().putStringState(flightId, json);
        ctx.getStub().setEvent("GateUpdated", json.getBytes());
        return json;
    }

    // ── Odczyt pojedynczego lotu → zwraca String ──────────────

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String queryFlight(final Context ctx, final String flightId) {
        return serialize(getFlightOrThrow(ctx, flightId));
    }

    // ── Odczyt wszystkich lotów → zwraca String (JSON array) ──

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String queryAllFlights(final Context ctx) {
        QueryResultsIterator<KeyValue> results =
                ctx.getStub().getStateByRange("", "");

        List<FlightData> flights = new ArrayList<>();
        for (KeyValue result : results) {
            String json = result.getStringValue();
            if (json != null && !json.isEmpty()) {
                flights.add(deserialize(json));
            }
        }
        return serialize(flights);
    }

    // ── Historia zmian → zwraca String ────────────────────────

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getFlightHistory(final Context ctx, final String flightId) {
        String callerRole = getCallerRole(ctx);
        if (!callerRole.equals("ADMIN")) {
            throw new ChaincodeException("Brak uprawnien: historia dostepna tylko dla ADMIN", "UNAUTHORIZED");
        }

        QueryResultsIterator<KeyModification> history =
                ctx.getStub().getHistoryForKey(flightId);

        List<String> records = new ArrayList<>();
        for (KeyModification mod : history) {
            records.add(String.format(
                    "{\"txId\":\"%s\",\"timestamp\":\"%s\",\"isDeleted\":%b,\"data\":%s}",
                    mod.getTxId(), mod.getTimestamp(), mod.isDeleted(), mod.getStringValue()
            ));
        }
        return "[" + String.join(",", records) + "]";
    }

    // ── Metody pomocnicze ──────────────────────────────────────

    private String getCallerRole(final Context ctx) {
        try {
            String role = ctx.getClientIdentity().getAttributeValue("role");
            return (role != null && !role.isEmpty()) ? role : "ADMIN";
        } catch (Exception e) {
            return "ADMIN";
        }
    }

    private FlightData getFlightOrThrow(final Context ctx, final String flightId) {
        String json = ctx.getStub().getStringState(flightId);
        if (json == null || json.isEmpty()) {
            throw new ChaincodeException("Lot " + flightId + " nie istnieje", "FLIGHT_NOT_FOUND");
        }
        return deserialize(json);
    }

    private void validateStatus(final String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new ChaincodeException(
                    "Nieprawidlowy status: " + status + ". Dozwolone: " + VALID_STATUSES,
                    "INVALID_STATUS"
            );
        }
    }

    private String serialize(final Object obj) {
        return MAPPER.writeValueAsString(obj);
    }

    private FlightData deserialize(final String json) {
        return MAPPER.readValue(json, FlightData.class);
    }
}