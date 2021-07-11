package org.bcnjug.jbcn.api.papers;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Sender {

    private String fullName;
    private String jobTitle;
    private String code;
    private String email;
    private String biography;
    private String company;
    private String picture;
    // social
    private String web;
    private String twitter;
    private String linkedin;
    // conference edition information
    private boolean travelCost;
    private boolean attendeesParty;
    private boolean speakersParty;
    private String tshirtSize;
    private String allergies;
    private boolean starred;
    // gdpr
    private DataConsent dataConsent;

}
