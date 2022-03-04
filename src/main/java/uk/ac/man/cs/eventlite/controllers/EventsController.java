package uk.ac.man.cs.eventlite.controllers;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.*;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private VenueService venueService;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}

	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id, Model model) {

		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		model.addAttribute("event", event);

		return "events/show";
	}
	
	@PatchMapping("/{id}")
public String updateEvent(@PathVariable("id") long id,
        @RequestParam("name") String name,
        @RequestParam("description") String description,
        @RequestParam("date") String date,
        @RequestParam("time") String time,
        @RequestParam("venue") String venueId){
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter time_formatter = DateTimeFormatter.ofPattern("hh:mm:ss");

        LocalDate localDate = LocalDate.parse(date,formatter);
        LocalTime localTime = LocalTime.parse(time,time_formatter);
        
		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		Venue venue = venueService.findById(Long.parseLong(venueId)).orElseThrow(() -> new EventNotFoundException(id));

		event.setName(name);
		event.setDescription(description);
		event.setDate(localDate);
		event.setTime(localTime);
		event.setVenue(venue);
		
		return venueId;
}
	@GetMapping
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());

		return "events/index";
	}
	
	@DeleteMapping("/{id}")
	public String deleteEvent(@PathVariable("id") long id) {
		if (!eventService.existsById(id)) {
			throw new EventNotFoundException(id);
		}
		eventService.deleteById(id);
		return "redirect:/events";
	}

}
