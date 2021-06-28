package org.bcnjug.jbcn.api.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bcnjug.jbcn.api.model.Tracked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Document(collection = "users")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Tracked {

    @Id
    private String id;

    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;

    @Indexed(unique = true)
    private String username;
    private String email;
    private Set<String> roles;

    private String password;
    private String salt;

}
