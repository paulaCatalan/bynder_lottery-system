package com.pcr.lottery_system.infrastructure.persistance;

import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.repository.LotteryEventRepository;
import com.pcr.lottery_system.infrastructure.JsonFileHandler;
import com.pcr.lottery_system.infrastructure.converter.LotteryEventConverter;
import com.pcr.lottery_system.infrastructure.dto.LotteryEventJson;
import com.pcr.lottery_system.infrastructure.dto.LotteryEventListWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Repository
public class JsonLotteryEventRepository implements LotteryEventRepository {
    private final File lotteryEventsFile;
    private final LotteryEventConverter converter;
    private final JsonFileHandler<LotteryEventJson, LotteryEventListWrapper> jsonFileHandler;


    public JsonLotteryEventRepository(
            @Value("${lottery_events.file.path:lottery_events.json}") String filePath,
            LotteryEventConverter converter,
            JsonFileHandler<LotteryEventJson, LotteryEventListWrapper> jsonFileHandler
    ) {
        this.converter = converter;
        this.jsonFileHandler = jsonFileHandler;

        this.lotteryEventsFile = Paths.get(filePath).toFile();

        try {
            this.jsonFileHandler.ensureFileExistsAndInitialized(this.lotteryEventsFile, LotteryEventListWrapper.class);
        } catch (IOException e) {
            System.err.println("Error initializing participants JSON file via handler: " + e.getMessage());
            throw new RuntimeException("Failed to initialize JSON repository", e);
        }
    }

    @Override
    public void save(LotteryEvent lotteryEvent) {
        try {
            List<LotteryEventJson> currentEvents = jsonFileHandler.readFromFile(lotteryEventsFile, LotteryEventListWrapper.class);
            LotteryEventJson lotteryEventJson = converter.toDto(lotteryEvent);
            currentEvents.add(lotteryEventJson);
            jsonFileHandler.writeToFile(lotteryEventsFile, currentEvents, LotteryEventListWrapper.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void findLotteryEventById(String id) {
        //TODO
    }

    @Override
    public void updateLotteryEvent(LotteryEvent lotteryEvent) {
        //TODO
    }
}
