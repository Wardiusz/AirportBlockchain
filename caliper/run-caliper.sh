#!/bin/bash
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   Hyperledger Caliper - Benchmark      ${NC}"
echo -e "${GREEN}========================================${NC}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ORG1_DIR="$PROJECT_ROOT/network/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com"
USER_MSP="$ORG1_DIR/users/User1@org1.example.com/msp"

# [1] Node.js
echo -e "\n${YELLOW}[1/5] Sprawdzam Node.js...${NC}"
if ! command -v node &> /dev/null; then
  echo -e "${RED}BLAD: Node.js nie jest zainstalowany! Wymagany Node 18+${NC}"
  exit 1
fi
echo -e "${GREEN}OK Node.js $(node --version)${NC}"

# [2] Siec
echo -e "\n${YELLOW}[2/5] Sprawdzam czy siec dziala...${NC}"
if ! docker ps --format '{{.Names}}' | grep -q "peer0.org1.example.com"; then
  echo -e "${RED}BLAD: Siec Fabric nie dziala! Uruchom ./network/scripts/start.sh${NC}"
  exit 1
fi
echo -e "${GREEN}OK Siec Fabric dziala${NC}"

# [3] Auto-wykrycie certyfikatu i klucza (dziala z CA i cryptogen)
echo -e "\n${YELLOW}[3/5] Wykrywam certyfikaty User1...${NC}"

# Certyfikat — bierz pierwszy plik z signcerts
CERT_FILE=$(find "$USER_MSP/signcerts" -type f -name '*.pem' | head -n1)
# Klucz prywatny — bierz pierwszy plik z keystore (hash przy CA / priv_sk przy cryptogen)
KEY_FILE=$(find "$USER_MSP/keystore" -type f | head -n1)
# Connection profile
CONN_PROFILE="$ORG1_DIR/connection-org1.yaml"

if [ -z "$CERT_FILE" ] || [ -z "$KEY_FILE" ]; then
  echo -e "${RED}BLAD: Nie znaleziono certyfikatu lub klucza w $USER_MSP${NC}"
  exit 1
fi
echo -e "  Cert: $CERT_FILE"
echo -e "  Key:  $KEY_FILE"
echo -e "${GREEN}OK Certyfikaty wykryte${NC}"

# Wygeneruj network config z aktualnymi sciezkami
cat > networks/airport-network.yaml << EOF
name: Airport Blockchain Network
version: "2.0.0"

caliper:
  blockchain: fabric

channels:
  - channelName: airportchannel
    contracts:
      - id: airport-cc

organizations:
  - mspid: Org1MSP
    identities:
      certificates:
        - name: User1
          clientPrivateKey:
            path: $KEY_FILE
          clientSignedCert:
            path: $CERT_FILE
    connectionProfile:
      path: $CONN_PROFILE
      discover: true
EOF
echo -e "${GREEN}OK Wygenerowano networks/airport-network.yaml${NC}"

# [4] Instalacja + binding
echo -e "\n${YELLOW}[4/5] Instaluje Caliper...${NC}"
if [ ! -d "node_modules" ]; then
  npm install
fi
npx caliper bind --caliper-bind-sut fabric:2.4
echo -e "${GREEN}OK Caliper gotowy${NC}"

# [5] Benchmark
echo -e "\n${YELLOW}[5/5] Uruchamiam benchmark...${NC}"
npx caliper launch manager \
  --caliper-workspace . \
  --caliper-networkconfig networks/airport-network.yaml \
  --caliper-benchconfig benchmarks/airport-config.yaml \
  --caliper-flow-only-test \
  --caliper-fabric-gateway-enabled

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}   OK Benchmark zakonczony!             ${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "  Raport HTML: $SCRIPT_DIR/report.html"