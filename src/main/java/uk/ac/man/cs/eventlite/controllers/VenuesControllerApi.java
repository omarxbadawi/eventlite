package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {
	
	private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";
	
	@Autowired
	private VenueService venueService;
	@Autowired
	private VenueModelAssembler venueAssembler;
	
	@Autowired
	private EventService eventService;
	@Autowired
	private EventModelAssembler eventAssembler;
	
	@ExceptionHandler(VenueNotFoundException.class)
	public ResponseEntity<?> venueNotFoundHandler(VenueNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(String.format(NOT_FOUND_MSG, ex.getMessage(), ex.getId()));
	}
	
	@GetMapping
	public CollectionModel<EntityModel<Venue>> getVenues(){
		Link profileLink = Link.of("http://localhost:8080/api/profile/venues");
		return venueAssembler.toCollectionModel(venueService.findAll())
				.add(profileLink.withRel("profile"))
				.add(linkTo(methodOn(VenuesControllerApi.class).getVenues()).withSelfRel());
	}
	
	@GetMapping("/{id}")
	public EntityModel<Venue> getVenue(@PathVariable("id") long id) {
		Venue greeting = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
		return venueAssembler.toModel(greeting);
	}
	
	@GetMapping("/{id}/events")
	public CollectionModel<EntityModel<Event>> getVenueEvents(@PathVariable("id") long id) {
		ArrayList<Event> events = new ArrayList<Event>();
		for(Event event : eventService.findAll()) {
			if(event.getVenue().getId() == id) {
				events.add(event);
			}
		}
		return eventAssembler.toCollectionModel(events)
				.add(linkTo(methodOn(VenuesControllerApi.class).getVenueEvents(id)).withSelfRel());
	}
	
	@GetMapping("/{id}/next3events")
	public CollectionModel<EntityModel<Event>> getVenueNext3Events(@PathVariable("id") long id) {
		ArrayList<Event> events = new ArrayList<Event>();
		for(Event event : eventService.findUpcoming()) {
			if(event.getVenue().getId() == id) {
				events.add(event);
				if(events.size() == 3) {
					break;
				}
			}
		}
		return eventAssembler.toCollectionModel(events)
				.add(linkTo(methodOn(VenuesControllerApi.class).getVenueNext3Events(id)).withSelfRel());
	}
}
