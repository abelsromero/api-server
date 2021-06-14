package org.bcnjug.jbcn.api.auth;

import com.nimbusds.jose.Header;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JwtGeneratorTest {

    static final int TTL_MILLIS = 60 * 60 * 1000;

    @Autowired
    JwtGenerator jwtGenerator;

    @Test
    void should_generate_jwt_token_without_roles() throws ParseException {
        final String subject = "the-subject";
        final LocalDateTime now = LocalDateTime.now();

        String token = jwtGenerator.createJWS(subject, Set.of(), TTL_MILLIS);
        JWT jwtToken = JWTParser.parse(token);

        Header header = jwtToken.getHeader();
        assertThat(header.getAlgorithm().getName()).isEqualTo("RS256");

        JWTClaimsSet claims = jwtToken.getJWTClaimsSet();
        assertThat(claims.getIssuer()).isEqualTo("https://www.jbcnconf.com");
        assertThat(claims.getSubject()).isEqualTo(subject);
        assertThat(claims.getStringArrayClaim("roles")).isNull();
        assertThat(claims.getExpirationTime()).isAfter(toDate(now)).isBefore(addTime(TTL_MILLIS, now));
    }

    @Test
    void should_generate_jwt_token() throws ParseException {
        final String subject = "the-subject";
        final LocalDateTime now = LocalDateTime.now();

        String token = jwtGenerator.createJWS(subject, Set.of("ADMIN", "VOTER"), TTL_MILLIS);
        JWT jwtToken = JWTParser.parse(token);

        Header header = jwtToken.getHeader();
        assertThat(header.getAlgorithm().getName()).isEqualTo("RS256");

        JWTClaimsSet claims = jwtToken.getJWTClaimsSet();
        assertThat(claims.getIssuer()).isEqualTo("https://www.jbcnconf.com");
        assertThat(claims.getSubject()).isEqualTo(subject);
        assertThat(claims.getStringArrayClaim("roles")).containsExactly("ADMIN", "VOTER");
        assertThat(claims.getExpirationTime()).isAfter(toDate(now)).isBefore(addTime(TTL_MILLIS, now));
    }

    private Date addTime(int ttlMillis, LocalDateTime now) {
        LocalDateTime plus = now.plus(ttlMillis, ChronoUnit.MILLIS);
        return toDate(plus);
    }

    private Date toDate(LocalDateTime plus) {
        return Date.from(plus.atZone(ZoneId.systemDefault()).toInstant());
    }
}
