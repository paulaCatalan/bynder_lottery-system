package com.pcr.lottery_system.domain.repository;

import com.pcr.lottery_system.domain.model.LotteryEvent;

public interface LotteryEventRepository {
    void save(LotteryEvent lotteryEvent);
    void findLotteryEventById(String lotteryEventId);
    void updateLotteryEvent(LotteryEvent lotteryEvent);
}
