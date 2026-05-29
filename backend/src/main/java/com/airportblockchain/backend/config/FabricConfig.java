package com.airportblockchain.backend.config;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.identity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

/**
 * FabricConfig — tworzy połączenie z siecią Hyperledger Fabric.
 *
 * Wszystkie ścieżki są czytane z application.yml,
 * który z kolei czyta wartości z pliku .env.
 */
@Configuration
public class FabricConfig {

    @Value("${fabric.mspId}")
    private String mspId;

    @Value("${fabric.certPath}")
    private String certPath;

    @Value("${fabric.keyPath}")
    private String keyPath;

    @Value("${fabric.tlsCertPath}")
    private String tlsCertPath;

    @Value("${fabric.peerEndpoint}")
    private String peerEndpoint;

    @Value("${fabric.peerHostname}")
    private String peerHostname;

    /**
     * Gateway — główny punkt wejścia do sieci Fabric.
     * Spring tworzy go raz przy starcie aplikacji (singleton).
     */
    @Bean
    public Gateway fabricGateway() throws IOException,
            CertificateException,
            InvalidKeyException {
        // 1. Kanał gRPC z TLS do peera
        ManagedChannel grpcChannel = newGrpcChannel();

        // 2. Tożsamość użytkownika (certyfikat X.509)
        Identity identity = newIdentity();

        // 3. Klucz prywatny do podpisywania transakcji
        Signer signer = newSigner();

        // 4. Zbuduj Gateway i połącz
        return Gateway.newInstance()
                .identity(identity)
                .signer(signer)
                .connection(grpcChannel)
                .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .submitOptions(options  -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .connect();
    }

    // ── Metody pomocnicze ──────────────────────────────────────

    private ManagedChannel newGrpcChannel() throws IOException {
        ChannelCredentials tlsCredentials = TlsChannelCredentials.newBuilder()
                .trustManager(Paths.get(tlsCertPath).toFile())
                .build();

        return Grpc.newChannelBuilder(peerEndpoint, tlsCredentials)
                .overrideAuthority(peerHostname)  // wymagane przez Fabric TLS
                .build();
    }

    private Identity newIdentity() throws IOException, CertificateException {
        // Odczytaj certyfikat PEM użytkownika
        var certReader = Files.newBufferedReader(Paths.get(certPath));
        var certificate = Identities.readX509Certificate(certReader);
        return new X509Identity(mspId, certificate);
    }

    private Signer newSigner() throws IOException, InvalidKeyException {
        // Klucz prywatny jest w folderze keystore — pobierz pierwszy plik
        Path keystoreDir = Paths.get(keyPath);
        Path privateKeyFile = Files.list(keystoreDir)
                .findFirst()
                .orElseThrow(() -> new IOException(
                        "Brak klucza prywatnego w: " + keyPath
                ));

        var keyReader = Files.newBufferedReader(privateKeyFile);
        var privateKey = Identities.readPrivateKey(keyReader);
        return Signers.newPrivateKeySigner(privateKey);
    }
}