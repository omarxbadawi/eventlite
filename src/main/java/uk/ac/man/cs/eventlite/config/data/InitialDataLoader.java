package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;

@Configuration
@Profile("default")
public class InitialDataLoader {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			if (venueService.count() > 0) {
				log.info("Database already populated with venues. Skipping venue initialization.");
				return;
			}
			// Build and save initial venues here.
			Venue venue = new Venue();
			venue.setId(1);
			venue.setName("Venue 1");
			venue.setRoad("23 Manchester Road");
			venue.setPostcode("E14 3BD");
			venue.setCapacity(100);
			venueService.save(venue);
			Venue venue2 = new Venue();
			venue2.setId(2);
			venue2.setName("Venue 2");
			venue2.setRoad("Highland Road");
			venue2.setPostcode("S43 2EZ");
			venue2.setCapacity(200);
			venueService.save(venue2);
			Venue venue3 = new Venue();
			venue3.setId(3);
			venue3.setName("Venue 3");
			venue3.setRoad("19 Acacia Avenue");
			venue3.setPostcode("WA15 8QY");
			venue3.setCapacity(300);
			venueService.save(venue3);

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
				return;
			}
			// Build and save initial events here.
			Event event1 = new Event();
			event1.setId(1);
			event1.setName("Event 1");
			event1.setDescription("This is the best event ever");
			event1.setVenue(venue);
			event1.setTime(LocalTime.now().minusHours(2));
			event1.setDate(LocalDate.now().minusDays(1));
			eventService.save(event1);
			Event event2 = new Event();
			event2.setId(2);
			event2.setName("Event 2");
			event2.setDescription("This is the second best event ever");
			event2.setVenue(venue2);
			event2.setTime(LocalTime.now().plusHours(1));
			event2.setDate(LocalDate.now().plusDays(1));
			eventService.save(event2);
			Event event3 = new Event();
			event3.setId(3);
			event3.setName("Event 3");
			event3.setDescription("This is the third best event ever");
			event3.setVenue(venue3);
			event3.setTime(LocalTime.now().plusHours(2));
			event3.setDate(LocalDate.now().plusDays(2));
			eventService.save(event3);
			Event event4 = new Event();
			event4.setId(4);
			event4.setName("Event 4");
			event4.setDescription("This is the third best event ever");
			event4.setVenue(venue3);
			event4.setTime(LocalTime.now().minusHours(5));
			event4.setDate(LocalDate.now().minusDays(1));
			eventService.save(event4);
			Event event5 = new Event();
			event5.setId(5);
			event5.setName("Event 5");
			event5.setDescription("This is the third best event ever");
			event5.setVenue(venue2);
			event5.setTime(LocalTime.now().plusHours(1));
			event5.setDate(LocalDate.now().plusDays(2));
			eventService.save(event5);
		};
	}
}
