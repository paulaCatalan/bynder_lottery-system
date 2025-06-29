package com.pcr.lottery_system.infrastructure.persistance;

import com.pcr.lottery_system.domain.exception.BallotAlreadyExistsException;
import com.pcr.lottery_system.domain.model.Ballot;
import com.pcr.lottery_system.domain.repository.BallotRepository;
import com.pcr.lottery_system.infrastructure.JsonFileHandler;
import com.pcr.lottery_system.infrastructure.converter.BallotConverter;
import com.pcr.lottery_system.infrastructure.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JsonBallotRepository implements BallotRepository {

    private final File ballotsFile;
    private final BallotConverter ballotConverter;
    private final JsonFileHandler<BallotJson, BallotListWrapper> jsonFileHandler;

    public JsonBallotRepository(
            @Value("${ballots.file.path:submitted_ballots.json}") String filePath,
            BallotConverter ballotConverter,
            JsonFileHandler<BallotJson, BallotListWrapper> jsonFileHandler
    ) {
        this.jsonFileHandler = jsonFileHandler;
        this.ballotConverter = ballotConverter;

        this.ballotsFile = Paths.get(filePath).toFile();

        try {
            this.jsonFileHandler.ensureFileExistsAndInitialized(this.ballotsFile, BallotListWrapper.class);
        } catch (IOException e) {
            System.err.println("Error initializing participants JSON file via handler: " + e.getMessage());
            throw new RuntimeException("Failed to initialize JSON repository", e);
        }
    }

    @Override
    public void save(Ballot ballot) {
        try {
            List<BallotJson> ballotsInFile = new ArrayList<>(jsonFileHandler.readFromFile(ballotsFile, BallotListWrapper.class));
            BallotJson ballotJson = ballotConverter.toDto(ballot);

            boolean exists = ballotsInFile.stream()
                    .anyMatch(ballotInFile -> ballotInFile.ballotId().equals(ballotJson.ballotId()));

            if (exists) {
                throw new BallotAlreadyExistsException(ballot.ballotId());
            } else {
                ballotsInFile.add(ballotJson);
            }
            jsonFileHandler.writeToFile(ballotsFile, ballotsInFile, BallotListWrapper.class);
        } catch (IOException e) {
            System.err.println("Error saving ballot: " + ballot.ballotId() + " - " + e.getMessage());
            throw new RuntimeException("Persistence error saving ballot", e);
        }
    }
}
