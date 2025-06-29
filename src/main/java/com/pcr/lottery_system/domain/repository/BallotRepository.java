package com.pcr.lottery_system.domain.repository;

import com.pcr.lottery_system.domain.model.Ballot;

import java.util.List;

public interface BallotRepository {
    void save(Ballot ballot);

    List<Ballot> findAllBallotsOfALottery(String lotteryId);
}


