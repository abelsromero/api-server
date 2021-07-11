package org.bcnjug.jbcn.api.auth;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtGenerator {

    private final RSAPrivateKey privateKey;

    private static final String issuer = "https://www.jbcnconf.com";

    public JwtGenerator(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @SneakyThrows
    public String createJWS(String subject, Set<String> roles, long ttlMillis) {

        LocalDateTime expirationTime = LocalDateTime.now().plus(ttlMillis, ChronoUnit.MILLIS);
        Instant instant = expirationTime.atZone(ZoneId.systemDefault()).toInstant();

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .issueTime(new Date())
                .expirationTime(Date.from(instant))
                .issuer(issuer)
                .subject(subject);

        if (!roles.isEmpty()) {
            claimsBuilder.claim("roles", roles.stream().sorted().collect(Collectors.toList()));
        }
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID("id01")
                .build();
        JWSObject jwsObject = new JWSObject(header, new Payload(claimsBuilder.build().toJSONObject()));
        jwsObject.sign(new RSASSASigner(privateKey));

        return jwsObject.serialize();
    }

    private String buildKid(byte[] key) {
        byte[] bytes = new byte[8];
        bytes[0] = key[0];
        bytes[1] = key[1];
        bytes[2] = key[2];
        bytes[3] = key[3];
        bytes[4] = key[4];
        bytes[5] = key[5];
        bytes[6] = key[6];
        bytes[7] = key[8];
        return Base64.getEncoder().encodeToString(bytes);
    }

}
