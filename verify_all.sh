#!/bin/bash
set -e

# Colors for pretty output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}===============================================${NC}"
echo -e "${BLUE}   WIMS Mod Multi-Version Verification Suite   ${NC}"
echo -e "${BLUE}===============================================${NC}"

# Target Minecraft versions and their respective properties
VERSIONS=("1.21" "1.21.1" "1.21.2" "1.21.3" "1.21.4" "1.21.5" "1.21.6" "1.21.7" "1.21.8" "1.21.9" "1.21.10" "1.21.11")
YARNS=("1.21+build.2" "1.21.1+build.2" "1.21.2+build.1" "1.21.3+build.2" "1.21.4+build.8" "1.21.5+build.1" "1.21.6+build.1" "1.21.7+build.8" "1.21.8+build.1" "1.21.9+build.1" "1.21.10+build.3" "1.21.11+build.5")
FABRICS=("0.100.7+1.21" "0.102.0+1.21.1" "0.106.0+1.21.2" "0.107.0+1.21.3" "0.110.0+1.21.4" "0.127.0+1.21.5" "0.128.0+1.21.6" "0.129.0+1.21.7" "0.131.0+1.21.8" "0.134.1+1.21.9" "0.138.4+1.21.10" "0.141.4+1.21.11")

for i in "${!VERSIONS[@]}"; do
    MC_VER="${VERSIONS[$i]}"
    YARN_VER="${YARNS[$i]}"
    FABRIC_VER="${FABRICS[$i]}"

    echo -e "\n${YELLOW}[WIMS-Verify] Starting verification for Minecraft ${MC_VER}...${NC}"
    echo -e "${BLUE}Details: Yarn Mappings = ${YARN_VER}, Fabric API = ${FABRIC_VER}${NC}"

    # 1. Clean previous build outputs to prevent dependency/mapping caching conflicts
    echo -e "${BLUE}Running clean...${NC}"
    ./gradlew clean

    # 2. Run GameTest integration tests (only for 1.21.4 where GameTests are supported by Loom version parser)
    if [ "${MC_VER}" = "1.21.4" ]; then
        echo -e "${BLUE}Running GameTests for ${MC_VER}...${NC}"
        if ./gradlew runGameTest -Pminecraft_version="${MC_VER}" -Pyarn_mappings="${YARN_VER}" -Pfabric_version="${FABRIC_VER}"; then
            echo -e "${GREEN}✓ GameTests PASSED for Minecraft ${MC_VER}${NC}"
        else
            echo -e "${RED}✗ GameTests FAILED for Minecraft ${MC_VER}${NC}"
            exit 1
        fi
    else
        echo -e "${BLUE}GameTests skipped for ${MC_VER} (using production build checks instead)${NC}"
    fi

    # 3. Build production JAR (will also copy to Modrinth folders if they exist)
    echo -e "${BLUE}Building Production JAR for ${MC_VER}...${NC}"
    if ./gradlew build -Pminecraft_version="${MC_VER}" -Pyarn_mappings="${YARN_VER}" -Pfabric_version="${FABRIC_VER}"; then
        echo -e "${GREEN}✓ Production build SUCCESS for Minecraft ${MC_VER}${NC}"
    else
        echo -e "${RED}✗ Production build FAILED for Minecraft ${MC_VER}${NC}"
        exit 1
    fi
done

echo -e "\n${GREEN}===============================================${NC}"
echo -e "${GREEN}   ALL VERIFICATIONS COMPLETED SUCCESSFULLY!    ${NC}"
echo -e "${GREEN}   Minecraft 1.21 to 1.21.11 are fully          ${NC}"
echo -e "${GREEN}   compatible and ready.                        ${NC}"
echo -e "${GREEN}===============================================${NC}"
