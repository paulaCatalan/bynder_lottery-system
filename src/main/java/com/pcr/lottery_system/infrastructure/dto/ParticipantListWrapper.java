package com.pcr.lottery_system.infrastructure.dto;

import java.util.List;

public class ParticipantListWrapper {
    private List<ParticipantJson> participants;

    public List<ParticipantJson> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantJson> participants) {
        this.participants = participants;
    }
}