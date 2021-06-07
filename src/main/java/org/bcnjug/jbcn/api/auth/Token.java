package org.bcnjug.jbcn.api.auth;

import lombok.Value;

@Value
public class Token {

    String access_token;
    String token_type;
    Long expires_in;
    
}
