package com.pcr.lottery_system.infrastructure.controller;


import com.pcr.lottery_system.application.LotteryEventService;
import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;
import com.pcr.lottery_system.infrastructure.dto.LotteryEventResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lotteries")

public class LotteryEventController {

    private final LotteryEventService lotteryEventService;

    public LotteryEventController(LotteryEventService lotteryEventService) {
        this.lotteryEventService = lotteryEventService;
    }



    @GetMapping("/open")
    public ResponseEntity<List<LotteryEventResponse>> getOpenLotteries() {

        List<LotteryEvent> openLotteries = lotteryEventService.findLotteryEventsByStatus(LotteryStatus.OPEN);

        List<LotteryEventResponse> response = openLotteries.stream()
                .map(this::mapToLotteryEventResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private LotteryEventResponse mapToLotteryEventResponse(LotteryEvent lotteryEvent) {
        return new LotteryEventResponse(
                lotteryEvent.id(),
                lotteryEvent.startTime(),
                lotteryEvent.endTime(),
                lotteryEvent.status(),
                lotteryEvent.winnerBallotId()
        );
    }
}
