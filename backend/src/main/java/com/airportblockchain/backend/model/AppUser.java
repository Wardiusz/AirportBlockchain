package com.airportblockchain.backend.model;

/**
 * @param password w produkcji: bcrypt hash
 */
public record AppUser(String username, String password, RoleUser role) {
}
