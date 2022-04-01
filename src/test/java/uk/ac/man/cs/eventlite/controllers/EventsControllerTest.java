package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;
	
	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;
	
	@MockBean
	private VenueService venueService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findPrevious();
		verify(eventService).findUpcoming();
		verifyNoInteractions(event);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));
		when(event.getVenue()).thenReturn(venue);

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findPrevious();
		verify(eventService).findUpcoming();
	}

	@Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/events/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found")).andExpect(handler().methodName("getEvent"));
	}
	
	@Test
	public void getNewEvent() throws Exception
	{
		Venue venue = new Venue();
		venue.setName("Venue");
		venue.setCapacity(1);
		
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));
		
		mvc.perform(get("/events/new").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
		.andExpect(view().name("events/new")).andExpect(handler().methodName("newEvent"));
		
	}
	@Test
	public void createEvent() throws Exception {

		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());
	
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Malevento")
				.param("time", "00:00")
				.param("description","Cool event")
				.param("venue.id", "0")
				.param("date", "3022-03-17")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound()).andExpect(content().string(""))
				.andExpect(view().name("redirect:/events")).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("createEvent"))
				.andExpect(flash().attributeExists("ok_message"));

		verify(eventService).save(arg.capture());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		LocalTime localTime = LocalTime.parse("00:00",dtf);
		LocalDate localDate = LocalDate.parse("3022-03-17", formatter);
		assertThat("Malevento", equalTo(arg.getValue().getName()));
		assertThat(localTime, equalTo(arg.getValue().getTime()));
		assertThat("Cool event", equalTo(arg.getValue().getDescription()));
		assertThat(0L, equalTo(arg.getValue().getVenue().getId()));
		assertThat(localDate, equalTo(arg.getValue().getDate()));
		
		
	}
	@Test
	public void postLongEventAttributes() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "xnuhzxcoushslzkqikhmwqlnkkzceknqwmojzmajsnhviatecuitfbeeuppivxgbpahzwniozgltkdfutsbjhpftkbowopuvsnapcfomtueknipodbikypkurgfsffbuaxylfvuxpbnnjgsecwqgqjaajyjlqtkhseqvrervvnlsfwdryyrsfsgxnegprkdntnnzztfairiexrxwceffpipgwswqkldituxnsldnposcgkgolqfgexmbmiblqaytrxwlieeasdtdfmcmwaetkxluglpfwxpxaescsbvfflba")
				.param("time", "00:00")
				.param("description","Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus")
				.param("venue.id", "0")
				.param("date", "3022-03-17")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(model().attributeHasFieldErrors("event", "name"))
				.andExpect(model().attributeHasFieldErrors("event", "description"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(any(Event.class));
	}
	
	@Test
	public void postEmptyNecessaryEventAttributes() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "")
				.param("time", "00:00")
				.param("description","Cool event")
				.param("venue.id", "")
				.param("date", "")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(model().attributeHasFieldErrors("event", "name"))
				.andExpect(model().attributeHasFieldErrors("event", "venue.id"))
				.andExpect(model().attributeHasFieldErrors("event", "date"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(any(Event.class));
	}
	
	@Test
	public void postEmptyUnnecessaryEventAttributes() throws Exception {
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());
	
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Malevento")
				.param("time", "")
				.param("description","")
				.param("venue.id", "0")
				.param("date", "3022-03-17")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound()).andExpect(content().string(""))
				.andExpect(view().name("redirect:/events")).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("createEvent"))
				.andExpect(flash().attributeExists("ok_message"));

		verify(eventService).save(arg.capture());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate localDate = LocalDate.parse("3022-03-17", formatter);
		assertThat("Malevento", equalTo(arg.getValue().getName()));
		assertThat(null, equalTo(arg.getValue().getTime()));
		assertThat("", equalTo(arg.getValue().getDescription()));
		assertThat(0L, equalTo(arg.getValue().getVenue().getId()));
		assertThat(localDate, equalTo(arg.getValue().getDate()));
	}
	
	@Test
	public void postPastEventDate() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Malevento")
				.param("time", "00:00")
				.param("description","Cool event")
				.param("venue.id", "0")
				.param("date", "1022-03-17")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(model().attributeHasFieldErrors("event", "date"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(any(Event.class));
	}
	
	@Test
	public void postPresentEventDate() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Malevento")
				.param("time", "00:00")
				.param("description","Cool event")
				.param("venue.id", "0")
				.param("date", LocalDate.now().toString())
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(model().attributeHasFieldErrors("event", "date"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(any(Event.class));
	}

	@Test
	public void deleteEvent() throws Exception {
		mvc.perform(delete("/events/4").with(user("Rob").roles(Security.ADMIN_ROLE))
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound()).andExpect(content().string(""))
				.andExpect(view().name("redirect:/events")).andExpect(model().hasNoErrors());
	}
	@Test
	public void getEvent() throws Exception {
		mvc.perform(get("/events/4").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/update")).andExpect(handler().methodName("updateR"));
	}

	

}
