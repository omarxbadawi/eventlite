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
			venue.setName("Mediacity");
			venue.setRoad("DPL Studio, University of Salford\n" +
					"\n" +
					"Mediacity Campus");
			venue.setPostcode("M50 2HE");
			venue.setCapacity(300);
			venue.setLongLat();
			venueService.save(venue);
			Venue venue2 = new Venue();
			venue2.setName("Binary Bar and Grill");
			venue2.setRoad("5 Arundel Street");
			venue2.setPostcode("M15 4JZ");
			venue2.setCapacity(50);
			venue2.setLongLat();
			venueService.save(venue2);
			Venue venue3 = new Venue();
			venue3.setName("Fairways Lodge");
			venue3.setRoad("George Street, Prestwich");
			venue3.setPostcode("M25 9WS");
			venue3.setCapacity(150);
			venue3.setLongLat();
			venueService.save(venue3);

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
				return;
			}
			// Build and save initial events here.
			Event event1 = new Event();
			event1.setName("TEDxUniversityofSalford");
			event1.setDescription("In the spirit of ideas worth spreading, TED has created a program called TEDx. TEDx is a program of local, self-organized events that bring people together to share a TED-like experience. Our event is a University event called TEDxUniversityofSalford.");
			event1.setVenue(venue);
			event1.setTime(LocalTime.of(18,0));
			event1.setDate(LocalDate.of(2022, 6, 18));
			eventService.save(event1);
			Event event2 = new Event();
			event2.setName("Castlefield Comedy Club");
			event2.setDescription("Here at Castlefield Comedy Club, we host some of the UK's best comedy talent, award-winning comics and future stars. Previous comedians have included (Live at The Apollo's ) Jonny Pelham, Brenna Reece, Tez Ilyas and Jack Carroll to name a few.");
			event2.setVenue(venue2);
			event2.setTime(LocalTime.of(19,30));
			event2.setDate(LocalDate.of(2022, 6, 5));
			eventService.save(event2);
			Event event3 = new Event();
			event3.setName("Fashion & Beauty Show");
			event3.setDescription("We’re bringing together the best local fashion and beauty businesses for an afternoon of demonstrations, talks, treatments, tips & tricks of the trade plus loads of local independent brands selling everything from jewellery and more!");
			event3.setVenue(venue3);
			event3.setTime(LocalTime.of(12,0));
			event3.setDate(LocalDate.of(2022, 6, 12));
			eventService.save(event3);
			Event event4 = new Event();
			event4.setName("Imola Grand Prix Screening");
			event4.setDescription("Join us in Italy themed fancy dress for the Imola GP. Prizes available for best costumes.");
			event4.setVenue(venue2);
			event4.setTime(LocalTime.of(14,0));
			event4.setDate(LocalDate.of(2022, 4, 24));
			eventService.save(event4);
			Event event5 = new Event();
			event5.setName("Makers Markets");
			event5.setDescription("The Makers Markets return to MediaCity. Discover the very finest local food, drink, art, design, vintage and modern craft carefully selected from Manchester artisans and businesses local to the North West.");
			event5.setVenue(venue);
			event5.setTime(LocalTime.of(10,0));
			event5.setDate(LocalDate.of(2022, 4, 15));
			eventService.save(event5);
		};
	}
}
