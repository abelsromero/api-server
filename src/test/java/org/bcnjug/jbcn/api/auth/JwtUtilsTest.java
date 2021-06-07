package org.bcnjug.jbcn.api.auth;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtUtilsTest {

    @Test
    public void should_generate_jwt_token() throws ParseException {
        final String issuer = "an-issuer";
        final String subject = "the-subject";
        final int ttlMillis = 60 * 60 * 1000;
        final LocalDateTime now = LocalDateTime.now();

        String token = JwtUtils.createJWS(subject, ttlMillis);

        assertThat(token)
                .isNotEmpty()
                .contains(".");

        JWTClaimsSet claims = JWTParser.parse(token).getJWTClaimsSet();
        assertThat(claims.getIssuer()).isEqualTo(issuer);
        assertThat(claims.getSubject()).isEqualTo(subject);
        assertThat(claims.getExpirationTime())
                .isAfter(toDate(now))
                .isBefore(addTime(ttlMillis, now));
    }

    @NotNull
    private Date addTime(int ttlMillis, LocalDateTime now) {
        LocalDateTime plus = now.plus(ttlMillis, ChronoUnit.MILLIS);
        return toDate(plus);
    }

    @NotNull
    private Date toDate(LocalDateTime plus) {
        return Date.from(plus.atZone(ZoneId.systemDefault()).toInstant());
    }
}
