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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerTest {

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
    public void getIndexWhenNoVenues() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

        mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

        verify(venueService).findAll();
        verifyNoInteractions(venue);
    }

    @Test
    public void getIndexWithVenues() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

        mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

        verify(venueService).findAll();
    }

    @Test
    public void getVenueNotFound() throws Exception {
        when(venueService.findById(99)).thenReturn(Optional.empty());
        mvc.perform(get("/venues/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
                .andExpect(view().name("venues/not_found")).andExpect(handler().methodName("getVenue"));
    }

    @Test
    public void getVenueFound() throws Exception {

        when(event.getVenue()).thenReturn(venue);
        when(venue.getId()).thenReturn(1L);
        when(eventService.findUpcoming()).thenReturn(Collections.<Event>singletonList(event));
        when(venueService.findById(1)).thenReturn(Optional.of(venue));
        mvc.perform(get("/venues/1").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("venues/show")).andExpect(handler().methodName("getVenue"));
    }

    @Test
    public void getNewVenue() throws Exception
    {

        mvc.perform(get("/venues/new").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("venues/new")).andExpect(handler().methodName("newVenue"));

    }
    @Test
    public void createVenue() throws Exception {

        ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);
        when(venueService.save(any(Venue.class))).then(returnsFirstArg());

        mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Test Venue")
                        .param("road", "10 Downing Street")
                        .param("postcode","SW1A")
                        .param("capacity", "314")
                        .accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound()).andExpect(content().string(""))
                .andExpect(view().name("redirect:/venues")).andExpect(model().hasNoErrors())
                .andExpect(handler().methodName("createVenue"))
                .andExpect(flash().attributeExists("ok_message"));

        verify(venueService).save(arg.capture());
        assertThat("Test Venue", equalTo(arg.getValue().getName()));
        assertThat("10 Downing Street", equalTo(arg.getValue().getRoad()));
        assertThat("SW1A", equalTo(arg.getValue().getPostcode()));
        assertThat(-0.127557, equalTo(arg.getValue().getLongitude()));
        assertThat(51.50333875, equalTo(arg.getValue().getLatitude()));
        assertThat(314, equalTo(arg.getValue().getCapacity()));


    }
    @Test
    public void postLongVenueAttributes() throws Exception {
        mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "xnuhzxcoushslzkqikhmwqlnkkzceknqwmojzmajsnhviatecuitfbeeuppivxgbpahzwniozgltkdfutsbjhpftkbowopuvsnapcfomtueknipodbikypkurgfsffbuaxylfvuxpbnnjgsecwqgqjaajyjlqtkhseqvrervvnlsfwdryyrsfsgxnegprkdntnnzztfairiexrxwceffpipgwswqkldituxnsldnposcgkgolqfgexmbmiblqaytrxwlieeasdtdfmcmwaetkxluglpfwxpxaescsbvfflba")
                        .param("road", "xnuhzxcoushslzkqikhmwqlnkkzceknqwmojzmajsnhviatecuitfbeeuppivxgbpahzwniozgltkdfutsbjhpftkbowopuvsnapcfomtueknipodbikypkurgfsffbuaxylfvuxpbnnjgsecwqgqjaajyjlqtkhseqvrervvnlsfwdryyrsfsgxnegprkdntnnzztfairiexrxwceffpipgwswqkldituxnsldnposcgkgolqfgexmbmiblqaytrxwlieeasdtdfmcmwaetkxluglpfwxpxaescsbvfflba")
                        .param("postcode","ABCDEFGHIJKLIMNOPQRSTUVWXYZ")
                        .param("capacity", "9999999999999999999999999999999999999999999999999999999999999")
                        .accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
                .andExpect(view().name("venues/new"))
                .andExpect(model().attributeHasFieldErrors("venue", "name"))
                .andExpect(model().attributeHasFieldErrors("venue", "road"))
                .andExpect(model().attributeHasFieldErrors("venue", "postcode"))
                .andExpect(model().attributeHasFieldErrors("venue", "capacity"))
                .andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

        verify(venueService, never()).save(any(Venue.class));
    }

    @Test
    public void postEmptyNecessaryVenueAttributes() throws Exception {
        mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("road", "")
                        .param("postcode","")
                        .param("capacity", "")
                        .accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
                .andExpect(view().name("venues/new"))
                .andExpect(model().attributeHasFieldErrors("venue", "name"))
                .andExpect(model().attributeHasFieldErrors("venue", "road"))
                .andExpect(model().attributeHasFieldErrors("venue", "postcode"))
                .andExpect(model().attributeHasFieldErrors("venue", "capacity"))
                .andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

        verify(venueService, never()).save(any(Venue.class));
    }

    @Test
    public void postZeroVenueCapacity() throws Exception {
        mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Test Venue")
                        .param("road", "10 Downing Street")
                        .param("postcode","SW1A")
                        .param("capacity", "0")
                        .accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
                .andExpect(view().name("venues/new"))
                .andExpect(model().attributeHasFieldErrors("venue", "capacity"))
                .andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

        verify(venueService, never()).save(any(Venue.class));
    }

    @Test
    public void postNegativeVenueCapacity() throws Exception {
        mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Test Venue")
                        .param("road", "10 Downing Street")
                        .param("postcode","SW1A")
                        .param("capacity", "-100")
                        .accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
                .andExpect(view().name("venues/new"))
                .andExpect(model().attributeHasFieldErrors("venue", "capacity"))
                .andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

        verify(venueService, never()).save(any(Venue.class));
    }



    @Test
    public void deleteUsedVenue() throws Exception {
        when(venueService.findById(2)).thenReturn(Optional.of(venue));
        when(event.getVenue()).thenReturn(venue);
        when(venue.getId()).thenReturn(2L);
        when(venueService.existsById(2)).thenReturn(true);
        when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));
        mvc.perform(delete("/venues/2").with(user("Rob").roles(Security.ADMIN_ROLE))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.TEXT_HTML).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/show")).andExpect(model().hasNoErrors());
    }

    @Test
    public void deleteVenueNotFound() throws Exception {
        when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
        when(venueService.findById(99)).thenReturn(Optional.empty());
        mvc.perform(delete("/venues/99").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isNotFound())
                .andExpect(view().name("venues/not_found")).andExpect(handler().methodName("deleteVenue"));
    }

    @Test
    public void deleteUnusedVenue() throws Exception {

        when(venueService.findById(1)).thenReturn(Optional.of(venue));
//        when(venueService.existsById(1)).thenReturn(true);
        when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
        mvc.perform(delete("/venues/1").with(user("Rob").roles(Security.ADMIN_ROLE))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.TEXT_HTML).with(csrf()))
                .andExpect(status().isFound()).andExpect(content().string(""))
                .andExpect(view().name("redirect:/venues")).andExpect(model().hasNoErrors());
    }

    @Test
    public void getVenueUpdate() throws Exception {

        when(venueService.findById(1)).thenReturn(Optional.of(venue));
        mvc.perform(get("/venues/update/1").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("venues/update/update")).andExpect(handler().methodName("updateVenuePage"));
    }

    @Test
    public void updateVenueValid() throws Exception {
        when(venueService.findById(1)).thenReturn(Optional.of(venue));
//        when(venueService.existsById(1)).thenReturn(true);
        mvc.perform(post("/venues/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Test Venue")
                .param("road", "10 Downing Street")
                .param("postcode","SW1A")
                .param("capacity", "314")
                .accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound()).andExpect(content().string(""))
                .andExpect(view().name("redirect:/venues")).andExpect(model().hasNoErrors())
                .andExpect(handler().methodName("updateVenue")).andExpect(flash().attributeExists("ok_message"));
    }

    @Test
    public void updateVenueInvalid() throws Exception {
        when(venueService.findById(1)).thenReturn(Optional.of(venue));
        mvc.perform(post("/venues/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("road", "")
                        .param("postcode","")
                        .param("capacity", "")
                        .accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
                .andExpect(view().name("venues/update/update"))
                .andExpect(model().attributeHasFieldErrors("venue", "name"))
                .andExpect(model().attributeHasFieldErrors("venue", "road"))
                .andExpect(model().attributeHasFieldErrors("venue", "postcode"))
                .andExpect(model().attributeHasFieldErrors("venue", "capacity"))
                .andExpect(handler().methodName("updateVenue"));
    }

    @Test
    public void searchVenue() throws Exception {

        when(venueService.findByNameContainingIgnoreCase("venue")).thenReturn(Collections.<Venue>singletonList(venue));
        mvc.perform(get("/venues/search/?query=venue").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("venues/index")).andExpect(handler().methodName("searchVenueByName"));
    }



}
