package com.pcr.lottery_system.infrastructure.persistance;

import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.infrastructure.converter.ParticipantConverter;
import com.pcr.lottery_system.infrastructure.dto.ParticipantJson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.mockito.Mockito.when;


class JsonParticipantRepositoryTest {

    @Mock
    ParticipantConverter mockedConverter;

    JsonParticipantRepository jsonParticipantRepository;

    private static final String INITIAL_DATA_RESOURCE_PATH = "src/test/resources/participants.json";
    private Path tempFilePath;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        tempFilePath = Files.createTempFile("test-participants", ".json");

        try (var inputStream = getClass().getClassLoader().getResourceAsStream(INITIAL_DATA_RESOURCE_PATH)) {
            if (inputStream == null) {
                System.err.println("Warning: Initial test data resource not found: " + INITIAL_DATA_RESOURCE_PATH + ". Initializing temp file with empty JSON wrapper.");
                Files.write(tempFilePath, "{\"participants\":[]}".getBytes());
            } else {
                Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        jsonParticipantRepository = new JsonParticipantRepository(tempFilePath.toString(), mockedConverter);
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(tempFilePath);
        } catch (IOException e) {
            System.err.println("Error deleting temporary test file: " + e.getMessage());
        }
    }

    @Test
    void shouldReturnNullIfEmailIsNotPresentInJsonFile(){
        Participant participant = jsonParticipantRepository.findByEmail("notPresentEmail@email.com");
        Assertions.assertNull(participant);
    }

    @Test
    void shouldSaveParticipantToJsonAndFindItById(){
        Participant participantToSave = new Participant("testId4", "test4@email.com", "Maria");
        ParticipantJson participantJsonToSave = new ParticipantJson("testId4", "test4@email.com", "Maria");

        when(mockedConverter.toDto(participantToSave)).thenReturn(participantJsonToSave);

        when(mockedConverter.toDomain(participantJsonToSave)).thenReturn(participantToSave);

        jsonParticipantRepository.save(participantToSave);
        Participant participantAfterSave = jsonParticipantRepository.findByEmail("test4@email.com");

        Assertions.assertEquals(participantToSave, participantAfterSave);
    }

}
