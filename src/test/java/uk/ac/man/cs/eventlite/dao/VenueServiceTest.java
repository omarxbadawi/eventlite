package uk.ac.man.cs.eventlite.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Iterator;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private VenueService venueService;

	@Test
	public void countTest() {
		long count = venueService.count();
		assertThat(3L, equalTo(count));
	}

	@Test
	public void findAllTest() {
		Iterable<Venue> venues = venueService.findAll();

		int count = 0;
		for(Venue venue : venues) {
			count++;
		}

		assertThat(3, equalTo(count));
		Iterator<Venue> iterator = venues.iterator();

		assertThat("Venue 1", equalTo(iterator.next().getName()));
		assertThat("Venue 2", equalTo(iterator.next().getName()));
		assertThat("Venue 3", equalTo(iterator.next().getName()));
	}


	@Test
	public void updateTest() {
		Venue venue = venueService.findById(1).get();

		venue.setName("new");
		venueService.save(venue);

		venue = venueService.findById(1).get();
		assertThat("new", equalTo(venue.getName()));
	}

	@Test
	public void findByIdTest() {
		Optional<Venue> venue = venueService.findById(1);

		assertThat(false, equalTo(venue.isEmpty()));
		assertThat("Venue 1", equalTo(venue.get().getName()));
	}

	@Test
	public void saveTest() {
		Venue newVenue = new Venue();

		Venue savedVenue = venueService.save(newVenue);

		assertThat(true, equalTo(venueService.existsById(savedVenue.getId())));
		assertThat(savedVenue, equalTo(venueService.findById(savedVenue.getId()).get()));
	}

	@Test
	public void deleteByIdTest() {
		venueService.deleteById(1);

		assertThat(false, equalTo(venueService.existsById(1)));
	}

	@Test
	public void existsByIdTest() {
		assertThat(true, equalTo(venueService.existsById(1)));
	}

	@Test
	public void searchOneTest() {
		Iterable<Venue> venues = venueService.findByNameContainingIgnoreCase("1");

		int count = 0;
		for(Venue venue : venues) {
			count++;
		}

		assertThat(1, equalTo(count));
		assertThat("Venue 1", equalTo(venues.iterator().next().getName()));

		venues = venueService.findByNameContainingIgnoreCase("4");
		assertThat(false, equalTo(venues.iterator().hasNext()));
	}

	public void searchAllTest() {
		Iterable<Venue> venues = venueService.findByNameContainingIgnoreCase("venue");

		int count = 0;
		for(Venue venue : venues) {
			count++;
		}

		assertThat(3, equalTo(count));
	}
}
