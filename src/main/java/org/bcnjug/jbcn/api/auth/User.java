package org.bcnjug.jbcn.api.auth;

import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "users")
@Value
public class User {

    @Id
    private String id;

    private String username;
    private String password;
    private Set<String> roles;
    
}
