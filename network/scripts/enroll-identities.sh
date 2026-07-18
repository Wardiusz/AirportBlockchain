#!/bin/bash
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Rejestracja tozsamosci z rolami (CA)  ${NC}"
echo -e "${GREEN}========================================${NC}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NETWORK_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
FABRIC_DIR="$NETWORK_DIR/fabric-samples"
TEST_NET="$FABRIC_DIR/test-network"

echo -e "  network-dir:    $NETWORK_DIR"
echo -e "  fabric-samples: $FABRIC_DIR"

# fabric-ca-client z fabric-samples/bin
export PATH="$FABRIC_DIR/bin:$PATH"

ORG1="$TEST_NET/organizations/peerOrganizations/org1.example.com"
CA_TLS_CERT="$TEST_NET/organizations/fabric-ca/org1/ca-cert.pem"
USERS_DIR="$ORG1/users"

# Sprawdź czy CA działa
echo -e "\n${YELLOW}[1/4] Sprawdzam Fabric CA...${NC}"
if ! command -v fabric-ca-client &> /dev/null; then
  echo -e "${RED}BLAD: fabric-ca-client nie znaleziony w $FABRIC_DIR/bin${NC}"
  echo "Czy siec zostala uruchomiona z flaga -ca? (start.sh)"
  exit 1
fi
if [ ! -f "$CA_TLS_CERT" ]; then
  echo -e "${RED}BLAD: brak certyfikatu CA: $CA_TLS_CERT${NC}"
  echo "Uruchom siec z CA: ./network/scripts/start.sh"
  exit 1
fi
echo -e "${GREEN}OK Fabric CA dostepne${NC}"

# Enroll bootstrap admina CA org1
echo -e "\n${YELLOW}[2/4] Loguje admina CA...${NC}"
export FABRIC_CA_CLIENT_HOME="$TEST_NET/organizations/fabric-ca/org1"
fabric-ca-client enroll \
  -u https://admin:adminpw@localhost:7054 \
  --caname ca-org1 \
  --tls.certfiles "$CA_TLS_CERT"
echo -e "${GREEN}OK Admin CA zalogowany${NC}"

# Funkcja: zarejestruj + enrolluj uzytkownika z atrybutem role
register_user() {
  local NAME=$1
  local SECRET=$2
  local ROLE=$3

  echo -e "  -> $NAME (role=$ROLE)"

  # Rejestracja z atrybutem role (:ecert = atrybut domyslnie w certyfikacie)
  fabric-ca-client register \
    --caname ca-org1 \
    --id.name "$NAME" \
    --id.secret "$SECRET" \
    --id.type client \
    --id.attrs "role=$ROLE:ecert" \
    --tls.certfiles "$CA_TLS_CERT" >/dev/null 2>&1 || true  # || true gdy juz istnieje

  local MSP_DIR="$USERS_DIR/$NAME@org1.example.com/msp"

  # Zadanie włączenia atrybutu role do certyfikatu
  fabric-ca-client enroll \
    -u "https://$NAME:$SECRET@localhost:7054" \
    --caname ca-org1 \
    -M "$MSP_DIR" \
    --enrollment.attrs "role" \
    --tls.certfiles "$CA_TLS_CERT" >/dev/null 2>&1

  # Skopiowanie config.yaml (NodeOUs) żeby MSP był poprawny
  cp "$ORG1/msp/config.yaml" "$MSP_DIR/config.yaml" 2>/dev/null || true
}

echo -e "\n${YELLOW}[3/4] Rejestruje uzytkownikow z rolami...${NC}"
register_user "airlineuser" "airlinepw" "AIRLINE"
register_user "handleruser" "handlerpw" "HANDLER"
register_user "adminuser"   "adminpw2"  "ADMIN"
echo -e "${GREEN}OK Uzytkownicy zarejestrowani${NC}"

echo -e "\n${YELLOW}[4/4] Weryfikacja...${NC}"
for u in airlineuser handleruser adminuser; do
  CERT="$USERS_DIR/$u@org1.example.com/msp/signcerts/cert.pem"
  if [ -f "$CERT" ]; then
    echo -e "  ${GREEN}OK${NC} $u -> $CERT"
  else
    echo -e "  ${RED}BRAK${NC} $u"
  fi
done

TARGET_UID="$(id -u "${SUDO_USER:-$USER}")"
TARGET_GID="$(id -g "${SUDO_USER:-$USER}")"
sudo chown -R "$TARGET_UID:$TARGET_GID" "$TEST_NET/organizations/"

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  OK Tozsamosci z rolami gotowe!        ${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "  Tozsamosci (MSP) w:"
echo "    $USERS_DIR/airlineuser@org1.example.com/msp  (role=AIRLINE)"
echo "    $USERS_DIR/handleruser@org1.example.com/msp  (role=HANDLER)"
echo "    $USERS_DIR/adminuser@org1.example.com/msp    (role=ADMIN)"
echo ""