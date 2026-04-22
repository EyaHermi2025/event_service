package tn.esprit.eventservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventType;
import java.time.LocalDateTime;

@SpringBootApplication
@org.springframework.cloud.openfeign.EnableFeignClients
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadMockEvents(EventRepository eventRepository) {
        return args -> {
            if (eventRepository.count() == 0) {
                Event event1 = Event.builder()
                        .title("Spring Boot Workshop")
                        .type(EventType.WORKSHOP)
                        .startDate(LocalDateTime.now().plusDays(2))
                        .endDate(LocalDateTime.now().plusDays(2).plusHours(4))
                        .manifesto("Learn to build microservices with Spring Boot!")
                        .maxParticipants(50)
                        .status("ACTIVE")
                        .clubId(1L)
                        .estimatedCost(1500.0)
                        .build();

                Event event2 = Event.builder()
                        .title("Coding Hackathon")
                        .type(EventType.COMPETITION)
                        .startDate(LocalDateTime.now().plusDays(5))
                        .endDate(LocalDateTime.now().plusDays(6))
                        .manifesto("A 24-hour hackathon for all students.")
                        .maxParticipants(200)
                        .status("ACTIVE")
                        .clubId(1L)
                        .estimatedCost(4500.0)
                        .build();

                eventRepository.save(event1);
                eventRepository.save(event2);

                System.out.println("✅ Mock events inserted into database successfully! (Total Budget = 6000.0)");
            }
        };
    }
}
