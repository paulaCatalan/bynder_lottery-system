package com.pcr.lottery_system.infrastructure.persistance;

import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.domain.repository.ParticipantRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JsonParticipantRepository implements ParticipantRepository {
    @Override
    public Participant findByEmail(String email) {
        return null;
    }

    @Override
    public void save(Participant participant) {

    }
}
