package org.bcnjug.jbcn.api.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class JwkController {

    private final List<RSAPublicKey> publicKeys;

    public JwkController(List<RSAPublicKey> publicKeys) {
        this.publicKeys = publicKeys;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() {

        int id = 0;
        final List<JWK> jwks = new ArrayList<>();

        for (var key : publicKeys) {
            RSAKey.Builder builder = new RSAKey.Builder(key)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID("id0" + id);
            jwks.add(builder.build());
            id++;
        }

        return new com.nimbusds.jose.jwk.JWKSet(jwks)
                .toJSONObject();
    }
}
