package com.pcr.lottery_system.infrastructure.dto;

import java.util.List;

public class LotteryEventListWrapper {
    private List<LotteryEventJson> lotteryEvents;

    public List<LotteryEventJson> getLotteryEvents() {
        return lotteryEvents;
    }

    public void setLotteryEvents(List<LotteryEventJson> lotteryEvents) {
        this.lotteryEvents = lotteryEvents;
    }
}