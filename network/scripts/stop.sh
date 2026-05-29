#!/bin/bash
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}Zatrzymuję sieć Hyperledger Fabric...${NC}"
cd "$HOME/fabric-samples/test-network"
./network.sh down
echo -e "${GREEN}✓ Sieć zatrzymana i wyczyszczona${NC}"
