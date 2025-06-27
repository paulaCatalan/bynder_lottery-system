package com.pcr.lottery_system.infrastructure.converter;

import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.infrastructure.dto.ParticipantJson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ParticipantConverterTest {

    ParticipantConverter participantConverter = new ParticipantConverter();

    @Test
    void shouldConvertParticipantJsonToParticipant(){
       ParticipantJson participantJson = new ParticipantJson("participantId", "email", "name");
       Participant expectedParticipant = new Participant("participantId", "email", "name");

       Participant convertedParticipant = participantConverter.toDomain(participantJson);

       Assertions.assertEquals(expectedParticipant, convertedParticipant);
    }

    @Test
    void shouldConvertParticipantToParticipantJson(){
        Participant participant = new Participant("participantId", "email", "name");
        ParticipantJson expectedParticipantJson = new ParticipantJson("participantId", "email", "name");

        ParticipantJson convertedParticipantJson = participantConverter.toDto(participant);

        Assertions.assertEquals(expectedParticipantJson, convertedParticipantJson);
    }

}