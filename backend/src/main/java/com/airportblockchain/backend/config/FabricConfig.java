package com.airportblockchain.backend.config;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Network;
import org.hyperledger.fabric.client.identity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Each identity has role in certification X.509 (Fabric CA),
 */
@Configuration
public class FabricConfig {

    @Value("${fabric.mspId}")
    private String mspId;

    @Value("${fabric.usersDir}")
    private String usersDir;

    @Value("${fabric.tlsCertPath}")
    private String tlsCertPath;

    @Value("${fabric.peerEndpoint}")
    private String peerEndpoint;

    @Value("${fabric.peerHostname}")
    private String peerHostname;

    @Value("${fabric.channel}")
    private String channelName;

    @Value("${fabric.chaincode}")
    private String chaincodeName;

    private static final Map<String, String> ROLE_TO_USER = Map.of(
        "AIRLINE", "airlineuser@org1.example.com",
        "HANDLER", "handleruser@org1.example.com",
        "ADMIN",   "adminuser@org1.example.com"
    );

    @Bean
    public Map<String, Contract> roleContracts() throws Exception {
        Map<String, Contract> contracts = new HashMap<>();

        for (var entry : ROLE_TO_USER.entrySet()) {
            String role = entry.getKey();
            String userDir = entry.getValue();
            String mspPath = usersDir + "/" + userDir + "/msp";

            Gateway gateway = buildGateway(mspPath);
            Network network = gateway.getNetwork(channelName);
            contracts.put(role, network.getContract(chaincodeName));
        }

        return contracts;
    }


    private Gateway buildGateway(String mspPath) throws Exception {
        ManagedChannel grpcChannel = newGrpcChannel();
        Identity identity = newIdentity(mspPath);
        Signer signer = newSigner(mspPath);

        return Gateway.newInstance()
                .identity(identity)
                .signer(signer)
                .connection(grpcChannel)
                .evaluateOptions(o -> o.withDeadlineAfter(15, TimeUnit.SECONDS))
                .submitOptions(o  -> o.withDeadlineAfter(15, TimeUnit.SECONDS))
                .connect();
    }

    private ManagedChannel newGrpcChannel() throws IOException {
        ChannelCredentials tls = TlsChannelCredentials.newBuilder()
                .trustManager(Paths.get(tlsCertPath).toFile())
                .build();
        return Grpc.newChannelBuilder(peerEndpoint, tls)
                .overrideAuthority(peerHostname)
                .build();
    }

    private Identity newIdentity(String mspPath) throws Exception {
        Path certFile = firstFileIn(mspPath + "/signcerts");

        var reader = Files.newBufferedReader(certFile);
        var certificate = Identities.readX509Certificate(reader);

        return new X509Identity(mspId, certificate);
    }

    private Signer newSigner(String mspPath) throws Exception {
        Path keyFile = firstFileIn(mspPath + "/keystore");

        var reader = Files.newBufferedReader(keyFile);
        var privateKey = Identities.readPrivateKey(reader);

        return Signers.newPrivateKeySigner(privateKey);
    }

    private Path firstFileIn(String dir) throws IOException {
        try (Stream<Path> files = Files.list(Paths.get(dir))) {
            return files.filter(Files::isRegularFile)
                    .findFirst()
                    .orElseThrow(() -> new IOException("Brak pliku w: " + dir));
        }
    }
}
