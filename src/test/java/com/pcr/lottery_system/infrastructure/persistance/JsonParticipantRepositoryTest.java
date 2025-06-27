package com.pcr.lottery_system.infrastructure.persistance;

import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.infrastructure.converter.ParticipantConverter;
import com.pcr.lottery_system.infrastructure.dto.ParticipantJson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;


//TODO: Use temp files for tests so file is more clean and controlled
class JsonParticipantRepositoryTest {

    @Mock
    ParticipantConverter mockedConverter;

    JsonParticipantRepository jsonParticipantRepository;

    private static final String TEST_FILE_PATH = "src/test/resources/participants.json";

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        jsonParticipantRepository = new JsonParticipantRepository(TEST_FILE_PATH, mockedConverter);
    }

    @Test
    void shouldFindParticipantByEmailInJsonFile(){
        String expectedParticipantId = "testId";
        String expectedEmailString = "test@email.com";
        String expectedName = "Maria";

        Participant expectedParticipant = new Participant(expectedParticipantId, expectedEmailString, expectedName);

        ParticipantJson participantJsonFromFile = new ParticipantJson(expectedParticipantId, expectedEmailString, expectedName);

        when(mockedConverter.toDomain(participantJsonFromFile)).thenReturn(expectedParticipant);

        Participant participant = jsonParticipantRepository.findByEmail(expectedEmailString);

        Assertions.assertEquals(expectedParticipant, participant);
    }

    @Test
    void shouldReturnNullIfEmailIsNotPresentInJsonFile(){
        Participant participant = jsonParticipantRepository.findByEmail("notPresentEmail@email.com");
        Assertions.assertNull(participant);
    }

    @Test
    void shouldSaveParticipantToJson(){
        Participant participantToSave = new Participant("testId4", "test4@email.com", "Maria");
        ParticipantJson participantJsonToSave = new ParticipantJson("testId4", "test4@email.com", "Maria");

        when(mockedConverter.toDto(participantToSave)).thenReturn(participantJsonToSave);

        when(mockedConverter.toDomain(participantJsonToSave)).thenReturn(participantToSave);

        jsonParticipantRepository.save(participantToSave);

        Participant participantAfterSave = jsonParticipantRepository.findByEmail("test4@email.com");
        Assertions.assertEquals(participantToSave, participantAfterSave);
    }

}
