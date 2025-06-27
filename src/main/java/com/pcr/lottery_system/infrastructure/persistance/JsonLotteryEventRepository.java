package com.pcr.lottery_system.infrastructure.persistance;

import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.repository.LotteryEventRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JsonLotteryEventRepository implements LotteryEventRepository {

    @Override
    public void save(LotteryEvent lotteryEvent) {
        //TODO
    }

    @Override
    public void findLotteryEventById(String id) {
        //TODO
    }
}
