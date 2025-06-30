package com.pcr.lottery_system.infrastructure.persistance;// --- Infrastructure Layer: Repository Implementation (JSON File-Based) ---

import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.domain.repository.ParticipantRepository;
import com.pcr.lottery_system.infrastructure.JsonFileHandler;
import com.pcr.lottery_system.infrastructure.dto.ParticipantJson;
import com.pcr.lottery_system.infrastructure.dto.ParticipantListWrapper;
import com.pcr.lottery_system.infrastructure.converter.ParticipantConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Repository
public class JsonParticipantRepository implements ParticipantRepository{

    private final File participantsFile;
    private final ParticipantConverter converter;
    private final JsonFileHandler<ParticipantJson, ParticipantListWrapper> jsonFileHandler;

    public JsonParticipantRepository(
            @Value("${participants.file.path:participants.json}") String filePath,
            ParticipantConverter converter,
            JsonFileHandler<ParticipantJson, ParticipantListWrapper> jsonFileHandler
    ) {
        this.converter = converter;
        this.jsonFileHandler = jsonFileHandler;

        this.participantsFile = Paths.get(filePath).toFile();

        try {
            this.jsonFileHandler.ensureFileExistsAndInitialized(this.participantsFile, ParticipantListWrapper.class);
        } catch (IOException e) {
            System.err.println("Error initializing participants JSON file via handler: " + e.getMessage());
            throw new RuntimeException("Failed to initialize JSON repository", e);
        }
    }

    @Override
    public Participant findByEmail(String email) {
        try {
            List<ParticipantJson> allParticipants = jsonFileHandler.readFromFile(participantsFile, ParticipantListWrapper.class);
            for (ParticipantJson participantJson : allParticipants) {
                if (participantJson.email().equals(email)) {
                    return converter.toDomain(participantJson);
                }
            }
            return null;
        } catch (IOException e) {
            System.err.println("Error finding participant by email: " + email + " - " + e.getMessage());
            throw new RuntimeException("Persistence error finding participant by email", e);
        }
    }

    @Override
    public Participant findById(String participantId) {
        try {
            List<ParticipantJson> allParticipants = jsonFileHandler.readFromFile(participantsFile, ParticipantListWrapper.class);
            for (ParticipantJson participantJson : allParticipants) {
                if (participantJson.participant_id().equals(participantId)) {
                    return converter.toDomain(participantJson);
                }
            }
            return null;
        } catch (IOException e) {
            System.err.println("Error finding participant by email: " + participantId + " - " + e.getMessage());
            throw new RuntimeException("Persistence error finding participant by email", e);
        }
    }

    @Override
    public void save(Participant participant) {
        try {
            List<ParticipantJson> currentParticipants = jsonFileHandler.readFromFile(participantsFile, ParticipantListWrapper.class);
            ParticipantJson participantToSave = converter.toDto(participant);
            currentParticipants.add(participantToSave);

            jsonFileHandler.writeToFile(participantsFile, currentParticipants, ParticipantListWrapper.class);
        } catch (IOException e) {
            System.err.println("Error saving participant: " + participant.participantId() + " - " + e.getMessage());
            throw new RuntimeException("Persistence error saving participant", e);
        }
    }

    public void clearFile() {
        try {
            jsonFileHandler.writeToFile(participantsFile, Collections.emptyList(), ParticipantListWrapper.class);
        } catch (IOException e) {
            System.err.println("Error clearing ballots JSON file: " + e.getMessage());
            throw new RuntimeException("Failed to clear JSON file for ballots", e);
        }
    }
}
