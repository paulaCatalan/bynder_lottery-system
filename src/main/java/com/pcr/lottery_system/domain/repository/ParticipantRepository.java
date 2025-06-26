package com.pcr.lottery_system.domain.repository;

import com.pcr.lottery_system.domain.model.Participant;

public interface ParticipantRepository {

    public Participant findByEmail(String email);
    public void save(Participant participant);
}
