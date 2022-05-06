package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

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
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.ac.man.cs.eventlite.EventLite;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static final String SESSION_KEY = "JSESSIONID";

	@LocalServerPort
	private int port;

	private WebTestClient client;

	private int currentRows;

	@BeforeEach
	public void setup() {
		currentRows = countRowsInTable("events");
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Event 1"));
			assertThat(result.getResponseBody(), containsString("Event 2"));
			assertThat(result.getResponseBody(), containsString("Event 3"));
		});
	}


	@Test
	public void testSearchEvents() {
		client.get().uri("/events/search/?query=1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Event 1"));
			assertThat(result.getResponseBody(), not(containsString("Event 2")));
			assertThat(result.getResponseBody(), not(containsString("Event 3")));
		});
	}


	@Test
	public void getEventNotFound() {
		client.get().uri("/events/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}
	@Test
	public void getEvent() {
		client.get().uri("/events/4").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("Event 1"));
					assertThat(result.getResponseBody(), containsString("Venue 2"));
				});
	}


	@Test
	public void getNewEventWithUser() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/new")
				.accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("_csrf"));
					assertThat(result.getResponseBody(), containsString("Add Event"));
				});
	}

	@Test
	public void postEventNoUser() {
		String[] tokens = login();

		// Attempt to POST a valid event.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Event 4");
		form.add("date", LocalDate.of(2022, 12, 12).toString());
		form.add("venue", "1");

		// We don't set the session ID, so have no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/events").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/sign-in"));

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void postEventSensibleData() {
		String[] tokens = login();

		// Attempt to POST a valid event.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Event 4");
		form.add("date", LocalDate.of(2022, 12, 12).toString());
		form.add("venue", "1");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));

		// Check one row is added to the database.
		assertThat(currentRows + 1, equalTo(countRowsInTable("events")));
	}

	@Test
	public void postEventNoData() {
		String[] tokens = login();

		// Attempt to POST an empty event.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "");
		form.add("description", "");
		form.add("time", "");
		form.add("date", "");
		form.add("venue", "");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("A name must be provided"));
					assertThat(result.getResponseBody(), containsString("Event must have a date"));
				});

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}


	@Test
	public void postEventBadData() {
		String[] tokens = login();

		// Attempt to POST an invalid event.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "xnuhzxcoushslzkqikhmwqlnkkzceknqwmojzmajsnhviatecuitfbeeuppivxgbpahzwniozgltkdfutsbjhpftkbowopuvsnapcfomtueknipodbikypkurgfsffbuaxylfvuxpbnnjgsecwqgqjaajyjlqtkhseqvrervvnlsfwdryyrsfsgxnegprkdntnnzztfairiexrxwceffpipgwswqkldituxnsldnposcgkgolqfgexmbmiblqaytrxwlieeasdtdfmcmwaetkxluglpfwxpxaescsbvfflba");
		form.add("date", LocalDate.of(1025, 3, 17).toString());
		form.add("venue", "-1");


		// The session ID cookie holds our login credentials.
		client.post().uri("/events").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("The name must be 256 characters or less"));
					assertThat(result.getResponseBody(), containsString("The date provided must be in the future"));
				});

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	public void getUpdateEvent() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/update/4")
				.accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("Update Event"));
				});
	}

	@Test
	public void updateEventNoUser() {
		String[] tokens = login();

		// Attempt to POST a valid event.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Event 4");
		form.add("date", LocalDate.of(2022, 12, 12).toString());
		form.add("venue", "1");

		// We don't set the session ID, so have no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/events/update/4").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/sign-in"));

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void updateEventSensibleData() {
		String[] tokens = login();

		// Attempt to POST an invalid event.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Event 4");
		form.add("date", LocalDate.of(2022, 12, 12).toString());
		form.add("venue", "1");


		// The session ID cookie holds our login credentials.
		client.post().uri("/events/update/4").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));

		// Check nothing is added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	public void updateEventNoData() {
		String[] tokens = login();

		// Attempt to POST an empty event.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "");
		form.add("description", "");
		form.add("time", "");
		form.add("date", "");
		form.add("venue", "");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/update/4").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("A name must be provided"));
					assertThat(result.getResponseBody(), containsString("Event must have a date"));
				});

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}


	@Test
	public void updateEventBadData() {
		String[] tokens = login();

		// Attempt to POST an invalid event.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "xnuhzxcoushslzkqikhmwqlnkkzceknqwmojzmajsnhviatecuitfbeeuppivxgbpahzwniozgltkdfutsbjhpftkbowopuvsnapcfomtueknipodbikypkurgfsffbuaxylfvuxpbnnjgsecwqgqjaajyjlqtkhseqvrervvnlsfwdryyrsfsgxnegprkdntnnzztfairiexrxwceffpipgwswqkldituxnsldnposcgkgolqfgexmbmiblqaytrxwlieeasdtdfmcmwaetkxluglpfwxpxaescsbvfflba");
		form.add("date", LocalDate.of(1025, 3, 17).toString());
		form.add("venue", "-1");


		// The session ID cookie holds our login credentials.
		client.post().uri("/events/update/4").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("The name must be 256 characters or less"));
					assertThat(result.getResponseBody(), containsString("The date provided must be in the future"));
				});

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}


	private String[] login() {
		String[] tokens = new String[2];

		// Although this doesn't POST the log in form it effectively logs us in.
		// If we provide the correct credentials here, we get a session ID back which
		// keeps us logged in.
		EntityExchangeResult<String> result = client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get()
				.uri("/events").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
		tokens[0] = getCsrfToken(result.getResponseBody());
		tokens[1] = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		return tokens;
	}

	private String getCsrfToken(String body) {
		Matcher matcher = CSRF.matcher(body);

		// matcher.matches() must be called; might as well assert something as well...
		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}




//	@Test
//	public void testPostEvent() {
//		client.post().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound();
//	}

}
