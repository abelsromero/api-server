package org.bcnjug.jbcn.api.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
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

        JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
        JWSObject jwsObject = new JWSObject(header, new Payload(claimsBuilder.build().toJSONObject()));
        jwsObject.sign(new RSASSASigner(privateKey));

        return jwsObject.serialize();
    }

}
