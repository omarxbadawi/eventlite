package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@RestController
@RequestMapping(value = "/api/events", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class EventsControllerApi {

	private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

	@Autowired
	private EventService eventService;

	@Autowired
	private EventModelAssembler eventAssembler;

	@ExceptionHandler(EventNotFoundException.class)
	public ResponseEntity<?> eventNotFoundHandler(EventNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(String.format(NOT_FOUND_MSG, ex.getMessage(), ex.getId()));
	}

	@GetMapping("/{id}")
	public EntityModel<Event> getEvent(@PathVariable("id") long id) {
		Event greeting = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		return eventAssembler.toModel(greeting);
	}
	
	@RequestMapping(value = "/update/{id}", method = RequestMethod.GET)
	public EntityModel<Event> updateEventPage(@PathVariable("id") long id) {
		Event greeting = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		return eventAssembler.toModel(greeting);
	}

	@GetMapping
	public CollectionModel<EntityModel<Event>> getAllEvents() {
		return eventAssembler.toCollectionModel(eventService.findAll())
				.add(linkTo(methodOn(EventsControllerApi.class).getAllEvents()).withSelfRel());
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteEvent(@PathVariable("id") long id){
		if (!eventService.existsById(id)) {
			throw new EventNotFoundException(id);
		}
		eventService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	
//	@PatchMapping("/{id}")
//	public ResponseEntity<?> updateEvent(@PathVariable("id") long id){
//		if (!eventService.existsById(id)) {
//			throw new EventNotFoundException(id);
//		}
//		return null;
//	}
	
    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateEvent(@RequestBody @Valid Event event1, @PathVariable Long id) {

    	Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    	
    	event.setName(event1.getName());
		event.setDescription(event1.getDescription());
		event.setDate(event1.getDate());
		event.setTime(event1.getTime());
		event.setVenue(event1.getVenue());
		
    	EntityModel<Event> entity = eventAssembler.toModel(event);

        return ResponseEntity.created(entity.getRequiredLink(IanaLinkRelations.SELF).toUri()).build();
    }
  @RequestMapping(value = "/new", method = RequestMethod.GET)
    public ResponseEntity < ? > newEvent() {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

  @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity < ? > createEvent(@RequestBody @Valid Event event, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.unprocessableEntity().build();
        }
        eventService.save(event);
        URI x = linkTo(EventsControllerApi.class).slash(event.getId()).toUri();
        
        return ResponseEntity.created(x).build();
    }
}
