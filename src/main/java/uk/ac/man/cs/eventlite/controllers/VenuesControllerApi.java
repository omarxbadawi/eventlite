package uk.ac.man.cs.eventlite.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
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
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {
	@Autowired
	private VenueService venueService;
	@Autowired
	private VenueModelAssembler venueAssembler;
	
	@GetMapping
	public Map<String, Map> getVenues(){
		HashMap<String, Map> mainMap = new HashMap<>();
		HashMap<String, Iterable<Venue>> _embedded = new HashMap<>();
		HashMap<String, Map>  _links = new HashMap<>();
		HashMap<String, String>  self = new HashMap<>();
		HashMap<String, String>  profile = new HashMap<>();

		_embedded.put("venues", venueService.findAll());
		mainMap.put("_embedded", _embedded);
		self.put("href", "http://localhost:8080/api/venues");
		profile.put("href", "http://localhost:8080/api/profile/venues");
		_links.put("self", self);
		_links.put("profile", profile);
		mainMap.put("_links", _links);
		
		return mainMap;
	}
	@GetMapping("/{id}")
	public EntityModel<Venue> getVenue(@PathVariable("id") long id) {
		Venue greeting = venueService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		return venueAssembler.toModel(greeting);
	}
}
