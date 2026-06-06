package com.airportblockchain.backend.model;

public enum RoleUser {
    AIRLINE,   // linia lotnicza — tworzy loty, zmienia status
    HANDLER,   // obsługa naziemna — zmienia bramkę
    ADMIN      // pełny dostęp + historia
}
