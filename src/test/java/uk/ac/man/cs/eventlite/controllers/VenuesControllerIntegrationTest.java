package uk.ac.man.cs.eventlite.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static final String SESSION_KEY = "JSESSIONID";

	@LocalServerPort
	private int port;

	private WebTestClient client;

	private int currentRows;

	@BeforeEach
	public void setup() {
		currentRows = countRowsInTable("venue");
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Venue 1"));
			assertThat(result.getResponseBody(), containsString("Venue 2"));
			assertThat(result.getResponseBody(), containsString("Venue 3"));
		});
	}

	@Test
	public void testSearchVenues() {
		client.get().uri("/venues/search/?query=1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Venue 1"));
		});
	}

	@Test
	public void getVenueNotFound() {
		client.get().uri("/venues/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}
	@Test
	public void getVenue() {
		client.get().uri("/venues/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("Venue 1"));
					assertThat(result.getResponseBody(), containsString("23 Manchester Road"));
					assertThat(result.getResponseBody(), containsString("E14 3BD"));
					assertThat(result.getResponseBody(), containsString("100"));
				});
	}


	@Test
	public void getNewVenue() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/venues/new")
				.accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("_csrf"));
					assertThat(result.getResponseBody(), containsString("Add Venue"));
				});
	}

	@Test
	public void postVenueNoUser() {
		String[] tokens = login();

		// Attempt to POST a valid venue.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Venue 4");
		form.add("road", "5 Arundel Street");
		form.add("postcode", "M15 4JZ");
		form.add("capacity", "100");

		// We don't set the session ID, so have no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/sign-in"));

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venue")));
	}

	@Test
	@DirtiesContext
	public void postVenueSensibleData() {
		String[] tokens = login();

		// Attempt to POST a valid venue.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Venue 4");
		form.add("road", "5 Arundel Street");
		form.add("postcode", "M15 4JZ");
		form.add("capacity", "100");

		// The session ID cookie holds our login credentials.
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/venues"));

		// Check one row is added to the database.
		assertThat(currentRows + 1, equalTo(countRowsInTable("venue")));
	}

	@Test
	public void postVenueNoData() {
		String[] tokens = login();

		// Attempt to POST an invalid venue.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "");
		form.add("road", "");
		form.add("postcode", "");

		// The session ID cookie holds our login credentials.
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("Venue must have a name"));
					assertThat(result.getResponseBody(), containsString("Venue must have an address"));
					assertThat(result.getResponseBody(), containsString("Venue must have a postcode"));
				});

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venue")));
	}


	@Test
	public void postVenueBadData() {
		String[] tokens = login();

		// Attempt to POST an invalid venue.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "xnuhzxcoushslzkqikhmwqlnkkzceknqwmojzmajsnhviatecuitfbeeuppivxgbpahzwniozgltkdfutsbjhpftkbowopuvsnapcfomtueknipodbikypkurgfsffbuaxylfvuxpbnnjgsecwqgqjaajyjlqtkhseqvrervvnlsfwdryyrsfsgxnegprkdntnnzztfairiexrxwceffpipgwswqkldituxnsldnposcgkgolqfgexmbmiblqaytrxwlieeasdtdfmcmwaetkxluglpfwxpxaescsbvfflba");
		form.add("road", "xnuhzxcoushslzkqikhmwqlnkkzceknqwmojzmajsnhviatecuitfbeeuppivxgbpahzwniozgltkdfutsbjhpftkbowopuvsnapcfomtueknipodbikypkurgfsffbuaxylfvuxpbnnjgsecwqgqjaajyjlqtkhseqvrervvnlsfwdryyrsfsgxnegprkdntnnzztfairiexrxwceffpipgwswqkldituxnsldnposcgkgolqfgexmbmiblqaytrxwlieeasdtdfmcmwaetkxluglpfwxpxaescsbvfflba");
		form.add("postcode", "ABCDEFGHIJKLIMNOPQRSTUVWXYZ");
		form.add("capacity", "-5");


		// The session ID cookie holds our login credentials.
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("The name must be less than 256 characters"));
					assertThat(result.getResponseBody(), containsString("The address must be less than 300 characters"));
					assertThat(result.getResponseBody(), containsString("Postcode must be less than 10 characters"));
					assertThat(result.getResponseBody(), containsString("Venue capacity must be positive"));
				});

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venue")));
	}


	@Test
	public void getUpdateVenue() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/venues/update/1")
				.accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("Update Venue"));
				});
	}

	@Test
	public void updateVenueNoUser() {
		String[] tokens = login();

		// Attempt to POST a valid event.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Venue 4 Updated");
		form.add("road", "6 Arundel Street");
		form.add("postcode", "M16 4JZ");
		form.add("capacity", "200");

		// We don't set the session ID, so have no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/venues/update/1").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/sign-in"));

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venue")));
	}

	@Test
	@DirtiesContext
	public void updateVenueSensibleData() {
		String[] tokens = login();

		// Attempt to POST an invalid venue.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Venue 4 Updated");
		form.add("road", "6 Arundel Street");
		form.add("postcode", "M16 4JZ");
		form.add("capacity", "200");


		// The session ID cookie holds our login credentials.
		client.post().uri("/venues/update/1").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/venues"));

		// Check nothing is added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venue")));
	}

	@Test
	public void updateVenueNoData() {
		String[] tokens = login();

		// Attempt to POST an empty venue.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "");
		form.add("road", "");
		form.add("postcode", "");

		// The session ID cookie holds our login credentials.
		client.post().uri("/venues/update/1").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("Venue must have a name"));
					assertThat(result.getResponseBody(), containsString("Venue must have an address"));
					assertThat(result.getResponseBody(), containsString("Venue must have a postcode"));
				});

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venue")));
	}


	@Test
	public void updateVenueBadData() {
		String[] tokens = login();

		// Attempt to POST an invalid venue.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "xnuhzxcoushslzkqikhmwqlnkkzceknqwmojzmajsnhviatecuitfbeeuppivxgbpahzwniozgltkdfutsbjhpftkbowopuvsnapcfomtueknipodbikypkurgfsffbuaxylfvuxpbnnjgsecwqgqjaajyjlqtkhseqvrervvnlsfwdryyrsfsgxnegprkdntnnzztfairiexrxwceffpipgwswqkldituxnsldnposcgkgolqfgexmbmiblqaytrxwlieeasdtdfmcmwaetkxluglpfwxpxaescsbvfflba");
		form.add("road", "xnuhzxcoushslzkqikhmwqlnkkzceknqwmojzmajsnhviatecuitfbeeuppivxgbpahzwniozgltkdfutsbjhpftkbowopuvsnapcfomtueknipodbikypkurgfsffbuaxylfvuxpbnnjgsecwqgqjaajyjlqtkhseqvrervvnlsfwdryyrsfsgxnegprkdntnnzztfairiexrxwceffpipgwswqkldituxnsldnposcgkgolqfgexmbmiblqaytrxwlieeasdtdfmcmwaetkxluglpfwxpxaescsbvfflba");
		form.add("postcode", "ABCDEFGHIJKLIMNOPQRSTUVWXYZ");
		form.add("capacity", "-5");


		// The session ID cookie holds our login credentials.
		client.post().uri("/venues/update/1").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("The name must be less than 256 characters"));
					assertThat(result.getResponseBody(), containsString("The address must be less than 300 characters"));
					assertThat(result.getResponseBody(), containsString("Postcode must be less than 10 characters"));
					assertThat(result.getResponseBody(), containsString("Venue capacity must be positive"));
				});

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venue")));
	}


	private String[] login() {
		String[] tokens = new String[2];

		// Although this doesn't POST the log in form it effectively logs us in.
		// If we provide the correct credentials here, we get a session ID back which
		// keeps us logged in.
		EntityExchangeResult<String> result = client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get()
				.uri("/venues").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
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


}
