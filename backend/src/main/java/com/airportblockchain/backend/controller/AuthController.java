package com.airportblockchain.backend.controller;

import com.airportblockchain.backend.security.InMemoryUserStore;
import com.airportblockchain.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * POST /api/auth/login
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final InMemoryUserStore userStore;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        return userStore.findByUsername(username)
            .filter(user -> user.password().equals(password))
            .map(user -> ResponseEntity.ok(Map.of(
                "token",    jwtUtil.generateToken(user),
                "username", user.username(),
                "role",     user.role().name()
            )))
            .orElse(ResponseEntity.status(401)
                .body(Map.of("error", "Nieprawidlowe dane logowania")));
    }
}
