package com.pcr.lottery_system.infrastructure.controller;


import com.pcr.lottery_system.application.LotteryEventService;
import com.pcr.lottery_system.domain.model.Ballot;
import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;
import com.pcr.lottery_system.infrastructure.dto.LotteryEventResponse;
import com.pcr.lottery_system.infrastructure.dto.LotteryParticipationRequest;
import com.pcr.lottery_system.infrastructure.dto.LotteryParticipationResponse;
import com.pcr.lottery_system.infrastructure.dto.ParticipateInLotteryCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lotteries")

public class LotteryEventController {

    private final LotteryEventService lotteryEventService;

    public LotteryEventController(LotteryEventService lotteryEventService) {
        this.lotteryEventService = lotteryEventService;
    }

    @PostMapping("/participate")
    public ResponseEntity<LotteryParticipationResponse> participateInALottery(@RequestBody LotteryParticipationRequest request) {
        ParticipateInLotteryCommand participationCommand = new ParticipateInLotteryCommand(request.lotteryId(), request.participantId());
        Ballot ballot = lotteryEventService.participateInLotteryEvent(participationCommand);
        if (ballot != null) {
            if (ballot.ballotId() != null && !ballot.ballotId().isEmpty()) {
                LotteryParticipationResponse response = new LotteryParticipationResponse(ballot.ballotId(), "Participation submitted successfully");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
        }
        LotteryParticipationResponse response = new LotteryParticipationResponse(null, "Something went wrong. Participation NOT SUBMITTED.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

    }

    @GetMapping("/open")
    public ResponseEntity<List<LotteryEventResponse>> getOpenLotteries() {

        List<LotteryEvent> openLotteries = lotteryEventService.findLotteryEventsByStatus(LotteryStatus.OPEN);

        List<LotteryEventResponse> response = openLotteries.stream()
                .map(this::mapToLotteryEventResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/drawn/{lotteryDay}")
    public ResponseEntity<List<LotteryEventResponse>> getDrawnLotteriesForADay(@PathVariable LocalDate lotteryDay) {
            List<LotteryEvent> drawnLotteries = lotteryEventService.findDrawnLotteriesByDate(lotteryDay);

            List<LotteryEventResponse> response = drawnLotteries.stream()
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
