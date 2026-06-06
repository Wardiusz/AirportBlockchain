# Hyperledger Caliper — benchmark wydajnosci

To jest PRAWDZIWY Hyperledger Caliper (v0.6.0) — oficjalne narzedzie
do benchmarkingu blockchainow.

## Struktura

```
caliper/                          (umiesc obok folderu network/)
├── package.json                  zaleznosc: @hyperledger/caliper-cli
├── run-caliper.sh                skrypt uruchamiajacy (instalacja + binding + test)
├── networks/
│   └── airport-network.yaml      wskazuje na test-network (certyfikaty, kanal)
├── benchmarks/
│   └── airport-config.yaml       4 rundy testowe + Send Rate + monitoring Docker
└── workload/
    ├── createFlight.js           workload ZAPISU
    ├── queryFlight.js            workload ODCZYTU (pojedynczy lot)
    └── queryAllFlights.js        workload ODCZYTU (wszystkie loty)
```

WAZNE: folder `caliper/` musi byc OBOK `network/` (w glownym folderze projektu):
```
AirportBlockchain/
├── network/fabric-samples/test-network/...
└── caliper/   <- tutaj
```
Bo network-config.yaml uzywa sciezek wzglednych `../network/fabric-samples/...`

## Wymagania

- Node.js 18+ (Caliper to narzedzie Node.js)
- Uruchomiona siec Fabric (./network/scripts/start.sh)

## Uruchomienie (najprostsze)

```bash
cd caliper
chmod +x run-caliper.sh
./run-caliper.sh
```

Skrypt sam: sprawdzi Node i siec, zainstaluje Caliper, zrobi binding do Fabric 2.4,
uruchomi benchmark i wygeneruje raport.

## Uruchomienie (recznie, krok po kroku)

```bash
cd caliper

# 1. Instalacja
npm install

# 2. Binding do Fabric 2.4 (uzywa peer gateway — jak nasza aplikacja)
npx caliper bind --caliper-bind-sut fabric:2.4

# 3. Benchmark
npx caliper launch manager \
  --caliper-workspace . \
  --caliper-networkconfig networks/airport-network.yaml \
  --caliper-benchconfig benchmarks/airport-config.yaml \
  --caliper-flow-only-test \
  --caliper-fabric-gateway-enabled
```

## Wynik

Po zakonczeniu powstaje `report.html` — otworz w przegladarce.
Zawiera dla kazdej rundy:
- Success rate
- Throughput (TPS)
- Latency: min / max / avg / p50 / p95 / p99 (percentyle)
- Wykresy obciazenia CPU/RAM/sieci kontenerow (peer, orderer)

To gotowy material do rozdzialu o ewaluacji wydajnosci w pracy.

## Rundy w benchmarku (benchmarks/airport-config.yaml)

| Runda                      | Typ    | Send Rate         | Tx   |
|----------------------------|--------|-------------------|------|
| create-flight-fixed-25tps  | ZAPIS  | staly 25 TPS      | 250  |
| create-flight-ramping      | ZAPIS  | rosnacy 10->100   | 500  |
| query-flight               | ODCZYT | staly 200 TPS     | 1000 |
| query-all-flights          | ODCZYT | staly 100 TPS     | 500  |

Runda "ramping" (linear-rate) jest najlepsza do pokazania momentu nasycenia
sieci — Throughput przestaje rosnac mimo wzrostu Send Rate. Idealny wykres do pracy.

## Dostosowanie

W `benchmarks/airport-config.yaml` mozesz zmienic:
- `workers.number` — liczba rownoleglych procesow (wieksze obciazenie)
- `txNumber` — liczba transakcji w rundzie
- `rateControl.opts.tps` — docelowy Send Rate

## Caliper vs autorski benchmark

Masz teraz OBA:
- autorski (BlockchainBenchmarkTest.java) — prosty, wbudowany w testy Spring
- Caliper — branzowy standard, raport HTML, monitoring zasobow

W pracy mozesz uzyc Caliper jako glowne narzedzie ewaluacji i wspomniec ze
zweryfikowales wyniki wlasnym benchmarkiem (lub odwrotnie).
