package org.bcnjug.jbcn.api.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;
import org.bcnjug.jbcn.api.SecurityConfiguration;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class JwtUtils {

    private static final String issuer = "https://www.jbcnconf.com";

    @SneakyThrows
    public static String createJWS(String subject, long ttlMillis) {

        LocalDateTime expirationTime = LocalDateTime.now().plus(ttlMillis, ChronoUnit.MILLIS);
        Instant instant = expirationTime.atZone(ZoneId.systemDefault()).toInstant();

        // TODO add user information from UserDetails 
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("roles", List.of("ADMIN", "VOTER"))
                .issueTime(new Date())
                .expirationTime(Date.from(instant))
                .issuer(issuer)
                .subject(subject)
                .build();

        Payload payload = new Payload(claims.toJSONObject());

        JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);

        JWSObject jwsObject = new JWSObject(header, payload);
        RSAPrivateKey privateKey = SecurityConfiguration.readPrivateKey();
        RSASSASigner jwsSigner = new RSASSASigner(privateKey);
        jwsObject.sign(jwsSigner);

        return jwsObject.serialize();
    }

}
