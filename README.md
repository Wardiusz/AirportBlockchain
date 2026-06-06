# AirportBlockchain

> Description

---

## Backend
> Technologie: Spring Boot, JWT, Spring Security, gRPC transport, 


### Struktura plików

```
com/airportblockchain/backend/
├── config/
│   ├── FabricConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   └── FlightController.java
├── exception/
│   └── GlobalExceptionHandler.java
├── model/
│   ├── AppUser.java
│   ├── RoleUser.java
│   ├── ErrorResponse.java
│   ├── FlightData.java
│   ├── CreateFlightRequest.java
│   ├── UpdateStatusRequest.java
│   ├── UpdateGateRequest.java
│   └── ErrorResponse.java
├── security/
│   ├── InMemoryUserStore.java
│   ├── JwtUtil.java
│   └── JwtFilter.java
└── service/
    ├── FlightService.java
    └── impl/
        └── FlightServiceImpl.java
```

## Architektura

```
Uzytkownik loguje sie (JWT z rola)
   -> Spring wybiera tozsamosc Fabric Z TA ROLA w certyfikacie
   -> transakcja podpisana tym certyfikatem
   -> chaincode NIEZALEZNIE czyta role z certyfikatu i weryfikuje
   = podwojna weryfikacja (aplikacja + blockchain)
```


## Caliper

---

### Wymagania

- Node.js 18+
- Uruchomiona siec Fabric (./network/scripts/start.sh)

### Uruchomienie

```bash
cd caliper
chmod +x run-caliper.sh
./run-caliper.sh
```

Skrypt sam: sprawdzi Node i siec, zainstaluje Caliper, zrobi binding do Fabric 2.4,
uruchomi benchmark i wygeneruje raport.

### Wynik

Po zakonczeniu powstaje `report.html` — otworz w przegladarce.
Zawiera dla kazdej rundy:
- Success rate
- Throughput (TPS)
- Latency: min / max / avg / p50 / p95 / p99 (percentyle)
- Wykresy obciazenia CPU/RAM/sieci kontenerow (peer, orderer)

To gotowy material do rozdzialu o ewaluacji wydajnosci w pracy.

### Rundy w benchmarku (benchmarks/airport-config.yaml)

| Runda                      | Typ    | Send Rate         | Tx   |
|----------------------------|--------|-------------------|------|
| create-flight-fixed-25tps  | ZAPIS  | staly 25 TPS      | 250  |
| create-flight-ramping      | ZAPIS  | rosnacy 10->100   | 500  |
| query-flight               | ODCZYT | staly 200 TPS     | 1000 |
| query-all-flights          | ODCZYT | staly 100 TPS     | 500  |

Runda "ramping" (linear-rate) jest najlepsza do pokazania momentu nasycenia
sieci — Throughput przestaje rosnac mimo wzrostu Send Rate. Idealny wykres do pracy.

### Dostosowanie

W `benchmarks/airport-config.yaml` mozesz zmienic:
- `workers.number` — liczba rownoleglych procesow (wieksze obciazenie)
- `txNumber` — liczba transakcji w rundzie
- `rateControl.opts.tps` — docelowy Send Rate