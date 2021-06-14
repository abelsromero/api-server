package org.bcnjug.jbcn.api.papers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bcnjug.jbcn.api.model.Tracked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "papers")
public class Paper implements Tracked {

    @Id
    private String id;
    
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;

    private String title;
    private String edition;
    private List<Sender> senders;
    private String type;
    private String level;
    @JsonProperty("abstract")
    private String paperAbstract;
    private List<String> languages;

    private PaperState state;
    @Builder.Default
    private double averageVote = 0.0f;
    private int votesCount;
    private List<String> tags;
    private String preferenceDay;
    private String comments;
    private Boolean sponsor;

    private List<PaperVote> votes;


}
