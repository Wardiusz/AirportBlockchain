# -------------------------------------------------------------------
#  Dockerfile - Chaincode as a Service (CCaaS)
#  Builds chaincode as a standalone gRPC service.
#  Peers connect to this service instead of building their own image.
# -------------------------------------------------------------------

# Step 1: Building JAR using Gradle
FROM gradle:jdk25 AS build
WORKDIR /chaincode
COPY . .
RUN gradle installDist --no-daemon

# Step 2: Lightweight runtime image
FROM eclipse-temurin:25-jre
WORKDIR /chaincode

# Coping built chaincode
COPY --from=build /chaincode/build/install/airport-cc /chaincode

# Exposed port for chaincode
EXPOSE 9999

CMD ["./bin/airport-cc"]