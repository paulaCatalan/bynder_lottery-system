package com.pcr.lottery_system.infrastructure.dto;

import java.util.List;

public class BallotListWrapper {
    private List<BallotJson> ballots;

    public List<BallotJson> getBallots() {
        return ballots;
    }

    public void setBallots(List<BallotJson> ballots) {
        this.ballots = ballots;
    }
}