# AirportBlockchain

> This is a prototype built for a Bachelor's (engineering) thesis during Computer Science studies on [PJAIT](https://pja.edu.pl/en/) called: \
> **"A secure airport operational data sharing system using blockchain technology and role-based access control."** 

---

## Table of contents

1. [What it does](#1-what-it-does)
2. [Architecture](#2-architecture)
3. [Technologies](#3-technologies)
4. [Project structure](#4-project-structure)
5. [Requirements](#5-requirements)
6. [Getting started](#6-getting-started)
7. [Performance benchmark](#7-performance-benchmark-caliper)
8. [Terminating, stopping and restarting](#8-stopping-and-restarting)

## 1. What it does

AirportBlockchain enables secure sharing of airport operational data — flight
statuses, gates and routes — among multiple independent stakeholders: airlines,
ground handlers and airport management.

Data is stored on a **permissioned blockchain** (Hyperledger Fabric), providing a
shared, immutable single source of truth and a complete, auditable change history
without a trusted intermediary. Access to individual operations is restricted by
**roles enforced directly in the blockchain layer** (on-chain RBAC), not only in the
application.

The system targets operational and managerial data — it does not cover real-time
air traffic control (ATC) data.

---

## 2. Architecture

The system consists of three layers. Each communicates only through a defined
interface and forms a separate trust boundary.

```
┌────────────────────────────────────────────────┐
│               Frontend — Angular               │
│           login, role-dependent views          │
└───────────────────────┬────────────────────────┘
                        │ REST + JWT token
┌───────────────────────▼────────────────────────┐
│             Backend — Spring Boot              │
│  REST API, Spring Security, identity selection │
└───────────────────────┬────────────────────────┘
                        │ gRPC (Fabric Gateway SDK)
┌───────────────────────▼────────────────────────┐
│         Blockchain — Hyperledger Fabric        │
│       airport-cc chaincode, on-chain RBAC      │
└────────────────────────────────────────────────┘
```

### Two-layer access control (defense in depth)

```
User logs in  →  receives a JWT token carrying a role
Backend selects a Fabric identity  →  X.509 certificate with THAT role as an attribute
Transaction signed with the certificate
Chaincode INDEPENDENTLY reads the role from the certificate and verifies permissions
```

Permissions are checked in two independent places: in the application (JWT token)
and in the smart contract itself (the role attribute in the certificate). Even
bypassing the interface and the backend does not allow performing an operation
forbidden for a given role — the final decision is made by the contract based on a
cryptographically attested certificate.

---

## 3. Technologies

### Backend
- **Spring Boot** — REST API
- **Spring Security + JWT** (jjwt) — authentication and authorization in the application layer
- **Fabric Gateway SDK** (gRPC transport) — communication with the Fabric network
- **Java**

### Frontend
- **Angular 21** — standalone components
- **TypeScript**
- JWT interceptor (attaches the token to requests), route guards,
  role-dependent views

### Blockchain
- **Hyperledger Fabric 2.5** — permissioned network (two organizations)
- **Fabric CA** — identities with a role attribute embedded in the X.509 certificate
- **Chaincode written in Java** (`airport-cc`), Jackson serialization
- **Raft** consensus, chaincode deployed as **CCaaS** (Chaincode-as-a-Service)
- `airportchannel` channel

### Benchmark
- **Hyperledger Caliper 0.6** (binding `fabric:2.4`) — performance measurement

---

## 4. Project structure

```
AirportBlockchain/
├── network/          # Fabric network: scripts + fabric-samples/test-network
│   └── scripts/      # start.sh, enroll-identities.sh
├── chaincode/        # airport-cc smart contract (Java, Gradle)
├── backend/          # Spring Boot — REST API + Fabric integration
├── frontend/         # Angular
└── caliper/          # performance benchmark (Hyperledger Caliper)
```

Backend package structure:

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

---

## 5. Requirements

| Component | Notes |
|---|---|
| OS | Linux or Windows with WSL2 |
| Docker | running daemon (`docker ps` must work) |
| Hyperledger Fabric | binaries + `fabric-samples` (test-network) in `network/fabric-samples` |
| Java (JDK) | backend and chaincode |
| Gradle | building the chaincode |
| Node.js | 22 LTS (required by Angular 21) |
| Angular CLI | `npm install -g @angular/cli` |

---

## 6. Getting started

The order matters — each layer depends on the previous one:
**network → identities → backend → frontend**.

### 1. Fabric network + chaincode

Brings up the nodes (two organizations, orderer, CAs), creates the `airportchannel`
channel and deploys the `airport-cc` chaincode as CCaaS.

```bash
cd network/scripts
./start.sh
```

Verify (the following should be running: orderer, 2× peer, CAs, 2× chaincode CCaaS):

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

### 2. Role identities

Registers three identities with a role attribute in the X.509 certificate
(AIRLINE, HANDLER, ADMIN).

```bash
cd network/scripts
./enroll-identities.sh
```

> On native Linux, if the backend reports "permission denied" while reading the
> certificates, fix the file ownership:
> ```bash
> cd network/fabric-samples/test-network
> sudo chown -R "$(id -u):$(id -g)" organizations/
> ```

### 3. Backend

REST API + Fabric integration. Default port **8080**.

```bash
cd backend
./gradlew bootRun      # or: ./mvnw spring-boot:run
```

Verify login (returns a JWT token):

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 4. Frontend

Web interface. Default port **4200**.

```bash
cd frontend
npm install            # on first run / after dependency changes
ng serve
```

Open `http://localhost:4200`.


### Test accounts

Stored in application memory (prototype):

| Login    | Password   | Role    |
|----------|------------|---------|
| admin    | admin123   | ADMIN   |
| lot_user | airline123 | AIRLINE |
| handler1 | handler123 | HANDLER |

---

## 7. Performance benchmark (Caliper)

Requires a running Fabric network (step 1).

```bash
cd caliper
chmod +x run-caliper.sh
./run-caliper.sh
```

The script checks Node and the network, installs Caliper, binds to Fabric 2.4,
runs the benchmark and generates a `report.html` report (open it in a browser).
For each round the report contains: success rate, throughput (TPS) and latency
(min / max / avg / p50 / p95 / p99) along with CPU/RAM/network load charts for the
containers.

Benchmark rounds (`benchmarks/airport-config.yaml`):

| Round                     | Type  | Send Rate      | Tx   |
|---------------------------|-------|----------------|------|
| create-flight-fixed-25tps | WRITE | fixed 25 TPS   | 250  |
| create-flight-ramping     | WRITE | ramping 10→100 | 500  |
| query-flight              | READ  | fixed 200 TPS  | 1000 |
| query-all-flights         | READ  | fixed 100 TPS  | 500  |

The `ramping` round (linear-rate) shows the network saturation point — throughput
stops increasing despite a rising send rate.

---

## 8. Terminating, stopping and restarting

| Goal                  | Command                                 | Effect                          |
|-----------------------|-----------------------------------------|---------------------------------|
| Pause work            | `docker stop $(docker ps -q)`           | data in volumes is kept         |
| Tear down the network | `./network.sh down` (in `test-network`) | removes containers **and data** |

After a plain `docker stop`, the network resumes with `docker start` on the relevant
containers. The chaincode (CCaaS) containers are started with the `--rm` flag, so
they disappear once stopped and must be started again with the same `CHAINCODE_ID`
that `start.sh` printed during deployment.

## License
This project is licensed under the GPL-3.0 License.
