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
			} else {
				// Build and save initial venues here.
				Venue venue = new Venue();
				venue.setId(1);
				venue.setName("Venue 1");
				venue.setCapacity(100);
				venueService.save(venue);
				Venue venue2 = new Venue();
				venue2.setId(2);
				venue2.setName("Venue 2");
				venue2.setCapacity(200);
				venueService.save(venue2);
				Venue venue3 = new Venue();
				venue3.setId(3);
				venue3.setName("Venue 3");
				venue3.setCapacity(300);
				venueService.save(venue3);
			}

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
			} else {
				// Build and save initial events here.
				Event event1 = new Event();
				event1.setId(1);
				event1.setName("Event 1");
				event1.setVenue(1);
				event1.setTime(LocalTime.now());
				event1.setDate(LocalDate.now());
				eventService.save(event1);
				Event event2 = new Event();
				event2.setId(2);
				event2.setName("Event 2");
				event2.setVenue(2);
				event2.setTime(LocalTime.now().plusHours(1));
				event2.setDate(LocalDate.now().plusDays(1));
				eventService.save(event2);
				Event event3 = new Event();
				event3.setId(3);
				event3.setName("Event 3");
				event3.setVenue(3);
				event3.setTime(LocalTime.now().plusHours(2));
				event3.setDate(LocalDate.now().plusDays(2));
				eventService.save(event3);
			}
		};
	}
}
