package com.gbsw.snapy.infra.apns;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(ApnsProperties.class)
public class ApnsConfig {

    @Bean
    @ConditionalOnProperty(prefix = "apns", name = "enabled", havingValue = "true")
    public ApnsClient apnsClient(ApnsProperties properties) throws Exception {
        String apnsServer = properties.isProduction()
                ? ApnsClientBuilder.PRODUCTION_APNS_HOST
                : ApnsClientBuilder.DEVELOPMENT_APNS_HOST;
        File certificateFile = new File(properties.getCertificatePath());

        return new ApnsClientBuilder()
                .setApnsServer(apnsServer)
                .setClientCredentials(
                        loadCertificate(certificateFile),
                        loadPrivateKey(certificateFile),
                        properties.getCertificatePassword().isBlank() ? null : properties.getCertificatePassword()
                )
                .build();
    }

    @Bean
    public Executor apnsResponseExecutor() {
        return Executors.newFixedThreadPool(2);
    }

    private X509Certificate loadCertificate(File pemFile) throws Exception {
        try (var inputStream = Files.newInputStream(pemFile.toPath())) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(inputStream);
        }
    }

    private PrivateKey loadPrivateKey(File pemFile) throws Exception {
        String pem = Files.readString(pemFile.toPath(), StandardCharsets.UTF_8);
        String base64Key = extractPemBlock(pem, "PRIVATE KEY");
        byte[] keyBytes = Base64.getMimeDecoder().decode(base64Key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        try {
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception ignored) {
            return KeyFactory.getInstance("EC").generatePrivate(keySpec);
        }
    }

    private String extractPemBlock(String pem, String type) {
        String begin = "-----BEGIN " + type + "-----";
        String end = "-----END " + type + "-----";
        int beginIndex = pem.indexOf(begin);
        int endIndex = pem.indexOf(end);

        if (beginIndex < 0 || endIndex < 0 || endIndex <= beginIndex) {
            throw new IllegalArgumentException(type + " block not found in APNs PEM file.");
        }

        return pem.substring(beginIndex + begin.length(), endIndex)
                .replaceAll("\\s", "");
    }
}
