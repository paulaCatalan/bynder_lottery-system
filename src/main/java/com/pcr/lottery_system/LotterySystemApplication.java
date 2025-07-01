package com.pcr.lottery_system;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.pcr.lottery_system.domain.repository.ParticipantRepository;
import com.pcr.lottery_system.infrastructure.persistance.MongoParticipantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LotterySystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(LotterySystemApplication.class, args);
	}

	@Bean
	@Primary
	@Profile("!test")
	public ParticipantRepository mongoParticipantRepository(
			@Value("${mongodb.connection.string}") String connectionString,
			@Value("${mongodb.database.name}") String databaseName
	) {
		MongoDatabase database = MongoClients.create(connectionString).getDatabase(databaseName);
		return new MongoParticipantRepository(database);
	}

}
