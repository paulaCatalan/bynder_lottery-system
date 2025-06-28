package com.pcr.lottery_system.domain.repository;

import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;

import java.util.List;

public interface LotteryEventRepository {
    void save(LotteryEvent lotteryEvent);
    LotteryEvent findLotteryEventById(String lotteryEventId);
    void updateLotteryEvent(LotteryEvent lotteryEvent);
    List<LotteryEvent> findLotteryEventByStatus(LotteryStatus status);
}
