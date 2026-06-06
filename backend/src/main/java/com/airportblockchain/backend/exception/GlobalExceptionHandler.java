package com.airportblockchain.backend.exception;

import com.airportblockchain.backend.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        return ResponseEntity.badRequest().body(
            ErrorResponse.builder(400, "Validation Failed")
                .message("Nieprawidlowe dane wejsciowe")
                .path(request.getRequestURI())
                .details(details)
                .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse.builder(403, "Forbidden")
                .message("Brak uprawnien do wykonania tej operacji")
                .path(request.getRequestURI())
                .build()
        );
    }

    @ExceptionHandler(io.grpc.StatusRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleFabricError(
            io.grpc.StatusRuntimeException ex,
            HttpServletRequest request) {

        String message = extractFabricMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
            ErrorResponse.builder(502, "Blockchain Error")
                .message(message)
                .path(request.getRequestURI())
                .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse.builder(500, "Internal Server Error")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build()
        );
    }

    private String extractFabricMessage(String rawMessage) {
        if (rawMessage == null) return "Blad sieci blockchain";
        int lastComma = rawMessage.lastIndexOf(", ");
        if (lastComma != -1 && lastComma < rawMessage.length() - 2) {
            return rawMessage.substring(lastComma + 2);
        }
        return rawMessage;
    }
}
