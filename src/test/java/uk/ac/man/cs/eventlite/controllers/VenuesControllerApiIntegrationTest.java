package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringContains.containsString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$._links.self.href").value(endsWith("/api/venues"))
				.jsonPath("$._embedded.venues.length()").value(equalTo(3));
	}

	@Test
	public void getVenueNotFound() {
		client.get().uri("/venues/99").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$.error").value(equalTo("Could not find venue 99"));
	}
	
	@Test
	public void getVenue() {
		client.get().uri("/venues/1").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$._links.self.href").value(endsWith("/api/venues/1"))
				.jsonPath("$._links.venue.href").value(endsWith("/api/venues/1"))
				.jsonPath("$._links.events.href").value(endsWith("/api/venues/1/events"))
				.jsonPath("$._links.next3events.href").value(endsWith("/api/venues/1/next3events"))
				.jsonPath("$.name").value(equalTo("Venue 1"))
				.jsonPath("$.road").value(equalTo("23 Manchester Road"))
				.jsonPath("$.postcode").value(equalTo("E14 3BD"))
				.jsonPath("$.capacity").value(equalTo(100));
	}
	
	@Test
	public void getVenueEvents() {
		client.get().uri("/venues/2/events").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$._embedded.events.length()").value(equalTo(2));
	}
	
	@Test 
	public void getVenueNext3Events() {
		client.get().uri("/venues/2/next3events").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$._embedded.events.length()").value(equalTo(1));
	}
}
