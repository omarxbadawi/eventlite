package uk.ac.man.cs.eventlite.controllers;

import java.util.HashMap;
import java.util.Map;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {
	@Autowired
	private VenueService venueService;
	@Autowired
	private VenueModelAssembler venueAssembler;
	
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
}
