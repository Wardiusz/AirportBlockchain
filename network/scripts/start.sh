#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   Airport Blockchain - Start Network   ${NC}"
echo -e "${GREEN}========================================${NC}"

# ── Ścieżki ───────────────────────────────────────────────────
# start.sh → network/scripts/start.sh
# fabric-samples → network/fabric-samples/
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
NETWORK_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
FABRIC_DIR="$NETWORK_DIR/fabric-samples"
CHAINCODE_SRC="$PROJECT_ROOT/chaincode"
CHAINCODE_DEST="$FABRIC_DIR/airport-chaincode"

echo -e "  Projekt:        $PROJECT_ROOT"
echo -e "  fabric-samples: $FABRIC_DIR"

# [1/6] Zaleznosci
echo -e "\n${YELLOW}[1/6] Sprawdzam zależności...${NC}"
for cmd in docker git curl; do
  if ! command -v $cmd &> /dev/null; then
    echo -e "${RED}BŁĄD: '$cmd' nie jest zainstalowany!${NC}"
    exit 1
  fi
done
echo -e "${GREEN}✓ docker, git, curl - OK${NC}"

# [2/6] fabric-samples
echo -e "\n${YELLOW}[2/6] Sprawdzam fabric-samples...${NC}"
if [ ! -d "$FABRIC_DIR/test-network" ]; then
  echo -e "  Nie znaleziono test-network — pobieram fabric-samples..."

  if [ -d "$FABRIC_DIR" ]; then
    echo -e "  Usuwam niekompletną instalację..."
    rm -rf "$FABRIC_DIR"
  fi

  # Klonuj do network/fabric-samples
  git clone --depth 1 \
    https://github.com/hyperledger/fabric-samples.git \
    "$FABRIC_DIR"

  echo -e "  Pobieram binaries i obrazy Docker Fabric..."
  cd "$FABRIC_DIR"
  curl -sSL https://bit.ly/2ysbOFE | bash -s -- 2.5.9 1.5.12

  echo -e "${GREEN}✓ fabric-samples pobrane do network/${NC}"
else
  echo -e "${GREEN}✓ test-network już istnieje${NC}"
fi

# [3/6] Chaincode
echo -e "\n${YELLOW}[3/6] Kopiuję chaincode...${NC}"
rm -rf "$CHAINCODE_DEST"
cp -r "$CHAINCODE_SRC" "$CHAINCODE_DEST"

# Naprawa końcówek linii (CRLF -> LF) dla skryptu gradlew
sed -i -e 's/\r$//' "$CHAINCODE_DEST/gradlew"

chmod +x "$CHAINCODE_DEST/gradlew"

echo -e "${GREEN}✓ Chaincode skopiowany do $CHAINCODE_DEST${NC}"

# [4/6] Siec (z CA — wymagane dla rol w certyfikatach)
echo -e "\n${YELLOW}[4/6] Uruchamiam sieć Hyperledger Fabric...${NC}"
cd "$FABRIC_DIR/test-network"

./network.sh down 2>/dev/null || true
./network.sh up createChannel -c airportchannel -ca
echo -e "${GREEN}✓ Sieć uruchomiona, kanał 'airportchannel' utworzony${NC}"

# [5/6] Deploy chaincode jako serwis (CCaaS)
echo -e "\n${YELLOW}[5/6] Wdrazam chaincode 'airport-cc' jako serwis (CCaaS)...${NC}"
./network.sh deployCCAAS \
  -ccn airport-cc \
  -ccp "$CHAINCODE_DEST" \
  -c airportchannel

# [6/6] Rejestracja tozsamosci z rolami
echo -e "\n${YELLOW}[6/6] Rejestruje tozsamosci z rolami...${NC}"
if [ -f "$SCRIPT_DIR/enroll-identities.sh" ]; then
  chmod +x "$SCRIPT_DIR/enroll-identities.sh"
  "$SCRIPT_DIR/enroll-identities.sh"
else
  echo -e "${YELLOW}! Pominieto: brak enroll-identities.sh${NC}"
fi

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}            ✓ Siec gotowa!              ${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "  Peer endpoint : localhost:7051"
echo "  Kanal         : airportchannel"
echo "  Chaincode     : airport-cc (CCaaS)"
echo "  Tozsamosci    : airlineuser / handleruser / adminuser (z rolami)"
echo ""