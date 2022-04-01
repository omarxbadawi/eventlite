package uk.ac.man.cs.eventlite.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private VenueService venueService;
	
	private final Twitter twitter;
	
	public EventsController() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey("mCtNWKhduSQbhbRZiP3D0IokC")
		.setOAuthConsumerSecret("Peis1AdpBWbkySCIHCzfjjvKeHhrFK5TTPVWehO86LzSkcBtuM")
		.setOAuthAccessToken("1509547449953759234-9iX5BkMI7ODWJMm6K7bJwkDgTg6eGx")
		.setOAuthAccessTokenSecret("9ajbZRgiyqtGSHlIyWOnCOAfEZDczVTVc9mEKNbnTVJsP");
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
	}
	public String createTweet(String tweet) throws TwitterException {
	    Status status = twitter.updateStatus(tweet);
	    return status.getText();
	}
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

	private static class Tweet {
		Date date;
		String text;
		String link;

		Tweet(Date date, String text, String link) {
			this.date = date;
			this.text = text;
			this.link = link;
		}
		
		public Date getDate() {
			return this.date;
		}
		
		public String getText() {
			return this.text;
		}
		
		public String getLink() {
			return this.link;
		}
		
	}

	@GetMapping
	public String getAllEvents(Model model) {

//		model.addAttribute("events", eventService.findAll());
		model.addAttribute("previous", eventService.findPrevious());
		model.addAttribute("upcoming", eventService.findUpcoming());
		try {
			List<Tweet> timeline =  twitter.getUserTimeline().stream().limit(5)
					.map(item -> new Tweet(item.getCreatedAt(), item.getText(), "https://twitter.com/EventLite_G08/status/" + item.getId()))
					.collect(Collectors.toList());
			model.addAttribute("timeline", timeline);
		} catch (TwitterException e) {
			e.printStackTrace();
			model.addAttribute("timeline", new ArrayList<>());
		}

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


	@GetMapping("/search")
	public String searchEventByName(
			@RequestParam("query") String query, Model model) {
//		model.addAttribute("events", eventService.findByNameContainingIgnoreCase(query));
		model.addAttribute("previous", eventService.searchPrevious(query));
		model.addAttribute("upcoming", eventService.searchUpcoming(query));

		return "events/index";
	}

	@RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newEvent(Model model) {
        
    	if (!model.containsAttribute("event")) {
            model.addAttribute("event", new Event());
        }
        
        if (!model.containsAttribute("venues")) {
            model.addAttribute("venues", venueService.findAll());
        }
        
        return "events/new";
    }
    
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String createEvent(@RequestBody @Valid @ModelAttribute Event event, BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
        if (errors.hasErrors()) {
            model.addAttribute("event", event);
            model.addAttribute("venues", venueService.findAll());
            return "events/new";
        }

        eventService.save(event);
        redirectAttrs.addFlashAttribute("ok_message", "Added new event");
        
        return "redirect:/events";
    }
	@RequestMapping(value = "/update/{id}", method = RequestMethod.GET)
	public String updateEventPage(@PathVariable("id") long id, Model model){
		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		model.addAttribute("event", event);
		model.addAttribute("venues", venueService.findAll());
		return "events/update/update";
	}
    
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String updateEvent(@PathVariable("id") long id, @RequestBody @Valid @ModelAttribute Event event, BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
        if (errors.hasErrors()) {
            model.addAttribute("event", event);
            model.addAttribute("venues", venueService.findAll());
            return "events/update/update";
        }
        
		Event eventToUpdate = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));

        eventToUpdate.setName(event.getName());
        eventToUpdate.setDescription(event.getDescription());
        eventToUpdate.setDate(event.getDate());
        eventToUpdate.setTime(event.getTime());
        eventToUpdate.setVenue(event.getVenue());
        eventService.update(eventToUpdate);
        redirectAttrs.addFlashAttribute("ok_message", "Event updated");
        
        return "redirect:/events";
    }
}
