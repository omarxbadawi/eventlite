package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsControllerApi.class)
@Import({ Security.class, EventModelAssembler.class })
public class EventsControllerApiTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;

	@SpyBean
	private VenueModelAssembler venueAssembler;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

		verify(eventService).findAll();
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		Event e = new Event();
		e.setId(0);
		e.setName("Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		e.setVenue(new Venue());
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)));

		verify(eventService).findAll();
	}

	@Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/api/events/99").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("event 99"))).andExpect(jsonPath("$.id", equalTo(99)))
				.andExpect(handler().methodName("getEvent"));
	}
	
	@Test
	public void getEventVenueEventNotFound() throws Exception {
		mvc.perform(get("/api/events/99/venue").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("event 99"))).andExpect(jsonPath("$.id", equalTo(99)))
				.andExpect(handler().methodName("getEventVenue"));
	}
	@Test
	public void getEvent() throws Exception {
		
		Event e = new Event();
		Venue v = new Venue();
		v.setName("Cat");
		LocalDate date = LocalDate.parse("3022-03-17");
		LocalTime time = LocalTime.parse("00:00");
		e.setId(0);
		e.setName("Event");
		e.setDate(date);
		e.setTime(time);
		e.setVenue(v);
		e.setDescription("Description");
		when(eventService.findById(0)).thenReturn(Optional.of(e));
		

		mvc.perform(get("/api/events/0").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getEvent")).andExpect(jsonPath("$.length()", equalTo(7)))
				.andExpect(jsonPath("date", endsWith(date.toString())))
				.andExpect(jsonPath("time", endsWith(time.toString())))
				.andExpect(jsonPath("name", endsWith("Event")))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events/0")))
				.andExpect(jsonPath("$._links.event.href", endsWith("/api/events/0")))
				.andExpect(jsonPath("$._links.venue.href", endsWith("/api/events/0/venue")));

		verify(eventService).findById(0);
	}
	@Test
	public void getEventVenue() throws Exception {
		
		Event e = new Event();
		Venue v = new Venue();
		v.setName("Binary Bar and Grill");
		v.setCapacity(50);
		v.setId(0);
		v.setRoad("5 Arundel Street");
		v.setPostcode("M15 4JZ");
		//v.setLongLat();
		LocalDate date = LocalDate.parse("3022-03-17");
		LocalTime time = LocalTime.parse("00:00");
		e.setId(0);
		e.setName("Event");
		e.setDate(date);
		e.setTime(time);
		e.setVenue(v);
		e.setDescription("Description");
		when(eventService.findById(0)).thenReturn(Optional.of(e));
		when(venueService.findById(0)).thenReturn(Optional.of(v));

		mvc.perform(get("/api/events/0/venue").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getEventVenue")).andExpect(jsonPath("$.length()", equalTo(8)))
				.andExpect(jsonPath("name", endsWith("Binary Bar and Grill")))
				.andExpect(jsonPath("road", endsWith("5 Arundel Street")))
				.andExpect(jsonPath("postcode", endsWith("M15 4JZ")))
				.andExpect(jsonPath("longitude", equalTo(0.0)))
				.andExpect(jsonPath("latitude", equalTo(0.0)))
				.andExpect(jsonPath("capacity", equalTo(50)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/0")))
				.andExpect(jsonPath("$._links.venue.href", endsWith("/api/venues/0")))
				.andExpect(jsonPath("$._links.events.href", endsWith("/api/venues/0/events")))
				.andExpect(jsonPath("$._links.next3events.href", endsWith("/api/venues/0/next3events")))
				;

		verify(venueService).findById(0);
	}
}
