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
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Configuration
@Profile("test")
public class TestDataLoader {

	private final static Logger log = LoggerFactory.getLogger(TestDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
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
			
			// Build and save initial events here.
			Event event1 = new Event();
			event1.setId(1);
			event1.setName("Event 1");
			event1.setVenue(venue);
			event1.setTime(LocalTime.now().plusHours(1));
			event1.setDate(LocalDate.now());
			eventService.save(event1);
			Event event2 = new Event();
			event2.setId(2);
			event2.setName("Event 2");
			event2.setVenue(venue2);
			event2.setTime(LocalTime.now());
			event2.setDate(LocalDate.now().minusDays(1));
			eventService.save(event2);
			Event event3 = new Event();
			event3.setId(3);
			event3.setName("Event 3");
			event3.setVenue(venue3);
			event3.setTime(LocalTime.now());
			event3.setDate(LocalDate.now().plusDays(1));
			eventService.save(event3);
		};
	}
}
