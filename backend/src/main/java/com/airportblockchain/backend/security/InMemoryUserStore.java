package com.airportblockchain.backend.security;

import com.airportblockchain.backend.model.AppUser;
import com.airportblockchain.backend.model.RoleUser;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Hardcodowane role (dla uproszczenia systemu, jako rozbudowę możliwe zastąpienie bazą danych)
 */
@Component
public class InMemoryUserStore {

    private static final Map<String, AppUser> USERS = Map.of(
        "lot_user", new AppUser("lot_user", "airline123", RoleUser.AIRLINE),
        "handler1", new AppUser("handler1", "handler123", RoleUser.HANDLER),
        "admin",    new AppUser("admin",    "admin123",   RoleUser.ADMIN)
    );

    public Optional<AppUser> findByUsername(String username) {
        return Optional.ofNullable(USERS.get(username));
    }
}
