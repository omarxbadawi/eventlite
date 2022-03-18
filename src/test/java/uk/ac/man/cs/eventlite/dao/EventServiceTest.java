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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private EventService eventService;

	@Test
	public void countTest() {
		long count = eventService.count();
		assertThat(3L, equalTo(count));
	}
	
	@Test
	public void findPreviousTest() {
		Iterable<Event> events = eventService.findPrevious();
		
		int count = 0;
		for(Event event : events) {
			count++;
		}
		
		assertThat(1, equalTo(count));
		assertThat("Event 2", equalTo(events.iterator().next().getName()));
	}
	
	@Test
	public void findUpcomingTest() {
		Iterable<Event> events = eventService.findUpcoming();
		
		int count = 0;
		for(Event event : events) {
			count++;
		}
		
		assertThat(2, equalTo(count));
		
		Iterator<Event> iterator = events.iterator();
		assertThat("Event 1", equalTo(iterator.next().getName()));
		assertThat("Event 3", equalTo(iterator.next().getName()));
	}
	
	@Test
	public void findAllTest() {
		Iterable<Event> events = eventService.findAll();
		
		int count = 0;
		for(Event event : events) {
			count++;
		}
		
		assertThat(3, equalTo(count));
		
		Iterator<Event> iterator = events.iterator();
		assertThat("Event 2", equalTo(iterator.next().getName()));
		assertThat("Event 1", equalTo(iterator.next().getName()));
		assertThat("Event 3", equalTo(iterator.next().getName()));
	}
	
	@Test
	public void updateTest() {
		Event event = eventService.findById(4).get();
		
		event.setName("new");
		eventService.save(event);
		
		event = eventService.findById(4).get();
		assertThat("new", equalTo(event.getName()));
	}
	
	@Test
	public void findByIdTest() {
		Optional<Event> event = eventService.findById(4);
		
		assertThat(false, equalTo(event.isEmpty()));
		assertThat("Event 1", equalTo(event.get().getName()));
	}
	
	@Test
	public void saveTest() {
		Event newEvent = new Event();
		
		Event savedEvent = eventService.save(newEvent);
		
		assertThat(true, equalTo(eventService.existsById(savedEvent.getId())));
		assertThat(savedEvent, equalTo(eventService.findById(savedEvent.getId()).get()));
	}
	
	@Test
	public void deleteByIdTest() {
		eventService.deleteById(4);
		
		assertThat(false, equalTo(eventService.existsById(4)));
	}
	
	@Test
	public void existsByIdTest() {
		assertThat(true, equalTo(eventService.existsById(4)));
	}
	
	@Test
	public void searchUpcomingTest() {
		Iterable<Event> events = eventService.searchUpcoming("1");
		
		int count = 0;
		for(Event event : events) {
			count++;
		}
		
		assertThat(1, equalTo(count));
		assertThat("Event 1", equalTo(events.iterator().next().getName()));
		
		events = eventService.searchUpcoming("2");
		assertThat(false, equalTo(events.iterator().hasNext()));
	}
	
	@Test
	public void searchPreviousTest() {
		Iterable<Event> events = eventService.searchPrevious("2");
		
		int count = 0;
		for(Event event : events) {
			count++;
		}
		
		assertThat(1, equalTo(count));
		assertThat("Event 2", equalTo(events.iterator().next().getName()));
		
		events = eventService.searchPrevious("1");
		assertThat(false, equalTo(events.iterator().hasNext()));
	}
	
	public void findNameContainingIgnoreCaseTest() {
		Iterable<Event> events = eventService.findByNameContainingIgnoreCase("event");
		
		int count = 0;
		for(Event event : events) {
			count++;
		}
		
		assertThat(3, equalTo(count));
	}
}
