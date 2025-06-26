package com.pcr.lottery_system.infrastructure.persistance;

import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.infrastructure.converter.ParticipantConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;


//TODO: Use temp files for tests so file is more clean and controlled
class JsonParticipantRepositoryTest {

    JsonParticipantRepository jsonParticipantRepository = new JsonParticipantRepository(
            "src/test/resources/participants.json");

    @Test
    void shouldFindParticipantByEmailInJsonFile(){
        //TODO Try to make this test pass when save is implemented
        //I can save one here so it's more visual in the test
        Participant expectedParticipant = new Participant("testId", "test@email.com", "Maria");
        Participant participant = jsonParticipantRepository.findByEmail("test@email.com");
        Assertions.assertEquals(expectedParticipant, participant);
    }

    @Test
    void shouldReturnNullIfEmailIsNotPresentInJsonFile(){
        //I can save one here so it's more visual in the test
        Participant participant = jsonParticipantRepository.findByEmail("test1@email.com");
        Assertions.assertNull(participant);
    }

    @Test
    void shouldSaveParticipantToJson(){
        Participant participant = new Participant("testId4", "test4@email.com", "Maria");
        jsonParticipantRepository.save(participant);
        Participant participantAfterSave = jsonParticipantRepository.findByEmail("test4@email.com");
        Assertions.assertEquals(participant, participantAfterSave);




    }

}