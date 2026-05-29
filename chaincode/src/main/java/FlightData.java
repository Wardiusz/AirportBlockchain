import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/**
 * Model danych lotu przechowywany w ledgerze blockchain.
 * Każde pole jest niezmienialnie zapisywane przy każdej transakcji.
 */
@DataType
public final class FlightData {

    @Property
    @JsonProperty("flightId")
    private final String flightId;

    @Property
    @JsonProperty("airline")
    private final String airline;

    @Property
    @JsonProperty("origin")
    private final String origin;

    @Property
    @JsonProperty("destination")
    private final String destination;

    @Property
    @JsonProperty("gate")
    private final String gate;

    @Property
    @JsonProperty("status")
    private final String status;       // ON_TIME | DELAYED | CANCELLED | BOARDING | DEPARTED

    @Property
    @JsonProperty("scheduledDep")
    private final String scheduledDep; // ISO-8601: "2026-05-28T14:30:00Z"

    @Property
    @JsonProperty("lastUpdated")
    private final String lastUpdated;  // ISO-8601 timestamp ostatniej zmiany

    @Property
    @JsonProperty("updatedBy")
    private final String updatedBy;    // rola która zmieniła: AIRLINE | HANDLER | ADMIN

    // @JsonCreator mówi Jacksonowi żeby użył tego konstruktora przy deserializacji
    @JsonCreator
    public FlightData(
            @JsonProperty("flightId") String flightId,
            @JsonProperty("airline") String airline,
            @JsonProperty("origin") String origin,
            @JsonProperty("destination") String destination,
            @JsonProperty("gate") String gate,
            @JsonProperty("status") String status,
            @JsonProperty("scheduledDep") String scheduledDep,
            @JsonProperty("lastUpdated") String lastUpdated,
            @JsonProperty("updatedBy") String updatedBy
    ) {
        this.flightId = flightId;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.gate = gate;
        this.status = status;
        this.scheduledDep = scheduledDep;
        this.lastUpdated = lastUpdated;
        this.updatedBy = updatedBy;
    }

    public String getFlightId() {
        return flightId;
    }

    public String getAirline() {
        return airline;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getGate() {
        return gate;
    }

    public String getStatus() {
        return status;
    }

    public String getScheduledDep() {
        return scheduledDep;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
