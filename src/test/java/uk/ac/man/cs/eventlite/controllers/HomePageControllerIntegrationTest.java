package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
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
public class HomePageControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @BeforeEach
    public void setup() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    public void testGetAllEvents() {
        client.get().uri("/").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
    }

}