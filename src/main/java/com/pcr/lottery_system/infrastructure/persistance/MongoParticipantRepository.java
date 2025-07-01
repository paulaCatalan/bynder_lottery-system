package com.pcr.lottery_system.infrastructure.persistance;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.domain.repository.ParticipantRepository;
import org.bson.Document;

public class MongoParticipantRepository implements ParticipantRepository {

    private final MongoCollection<Document> collection;

    public MongoParticipantRepository(MongoDatabase database) {
        this.collection = database.getCollection("participants");
    }

    @Override
    public void save(Participant participant) {
        Document document = new Document()
                .append("id", participant.participantId())
                .append("_id", participant.participantId())
                .append("name", participant.name())
                .append("email", participant.email());
        collection.insertOne(document);
    }

    @Override
    public Participant findById(String id) {
        Document document = collection.find(new Document("_id", id)).first();
        if (document != null) {
            return new Participant(
                    document.getString("_id"),
                    document.getString("email"),
                    document.getString("name")
            );
        }
        return null;
    }

    @Override
    public Participant findByEmail(String email) {
        Document document = collection.find(new Document("email", email)).first();
        if (document != null) {
            return new Participant(
                    document.getString("_id"),
                    document.getString("email"),
                    document.getString("name")
            );
        }
        return null;
    }

}