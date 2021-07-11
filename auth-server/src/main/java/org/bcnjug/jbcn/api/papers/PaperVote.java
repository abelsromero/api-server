package org.bcnjug.jbcn.api.papers;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaperVote {

    private String userId;
    private String username;
    private Double vote;
    private LocalDateTime date;

    public static PaperVote of(String username, Double vote) {
        return new PaperVote(null, username, vote, null);
    }

}
