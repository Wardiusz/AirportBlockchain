package com.airportblockchain.backend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String timestamp;
    private final List<String> details;

    private ErrorResponse(Builder builder) {
        this.status = builder.status;
        this.error = builder.error;
        this.message = builder.message;
        this.path = builder.path;
        this.timestamp = Instant.now().toString();
        this.details = builder.details;
    }

    public static Builder builder(int status, String error) {
        return new Builder(status, error);
    }

    public static class Builder {
        private final int status;
        private final String error;
        private String message;
        private String path;
        private List<String> details;

        private Builder(int status, String error) {
            this.status = status;
            this.error  = error;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder details(List<String> d) {
            this.details = d;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}
