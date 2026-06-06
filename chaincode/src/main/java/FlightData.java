import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/**
 * Model danych lotu przechowywany w ledgerze blockchain.
 * Każde pole jest niezmienialnie zapisywane przy każdej transakcji.
 *
 * @param status       ON_TIME | DELAYED | CANCELLED | BOARDING | DEPARTED
 * @param scheduledDep ISO-8601: "2026-05-28T14:30:00Z"
 * @param lastUpdated  ISO-8601 timestamp ostatniej zmiany
 * @param updatedBy    rola która zmieniła: AIRLINE | HANDLER | ADMIN
 */
@DataType
public record FlightData(
        @Property @JsonProperty("flightId") String flightId,
        @Property @JsonProperty("airline") String airline,
        @Property @JsonProperty("origin") String origin,
        @Property @JsonProperty("destination") String destination,
        @Property @JsonProperty("gate") String gate, @Property @JsonProperty("status") String status,
        @Property @JsonProperty("scheduledDep") String scheduledDep,
        @Property @JsonProperty("lastUpdated") String lastUpdated,
        @Property @JsonProperty("updatedBy") String updatedBy
) {

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

    @Override
    public String flightId() {
        return flightId;
    }

    @Override
    public String airline() {
        return airline;
    }

    @Override
    public String origin() {
        return origin;
    }

    @Override
    public String destination() {
        return destination;
    }

    @Override
    public String gate() {
        return gate;
    }

    @Override
    public String status() {
        return status;
    }

    @Override
    public String scheduledDep() {
        return scheduledDep;
    }

    @Override
    public String lastUpdated() {
        return lastUpdated;
    }

    @Override
    public String updatedBy() {
        return updatedBy;
    }
}
