package com.pcr.lottery_system.infrastructure.converter;

import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.infrastructure.dto.ParticipantJson;
import org.springframework.stereotype.Component;

@Component
public class ParticipantConverter {

    public ParticipantJson toDto(Participant participant) {
        if (participant == null) {
            return null;
        }
        return new ParticipantJson(
                participant.participantId(),
                participant.email(),
                participant.name()
        );
    }

    public Participant toDomain(ParticipantJson participantJson) {
        if (participantJson == null) {
            return null;
        }
        return new Participant(
                participantJson.participant_id(),
                participantJson.email(),
                participantJson.name()
        );
    }
}

