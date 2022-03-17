package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

import javax.validation.Valid;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	@Autowired
	private VenueService venueService;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "venues/not_found";
	}

	@GetMapping("/{id}")
	public String getVenue(@PathVariable("id") long id, Model model) {

		Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
		model.addAttribute("venue", venue);

		return "venues/show";
	}
	
	@GetMapping
	public String getAllVenues(Model model) {

		model.addAttribute("venues", venueService.findAll());

		return "venues/index";
	}

	
	@DeleteMapping("/{id}")
	public String deleteVenue(@PathVariable("id") long id) {
		// todo only delete if no events have this venue
		if (!venueService.existsById(id)) {
			throw new EventNotFoundException(id);
		}
		venueService.deleteById(id);
		return "redirect:/venues";
	}


	@GetMapping("/search")
	public String searchVenueByName(
			@RequestParam("query") String query, Model model) {
		model.addAttribute("venues", venueService.findByNameContainingIgnoreCase(query));

		return "venues/index";
	}

	@RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newVenue(Model model) {
        
    	if (!model.containsAttribute("venue")) {
            model.addAttribute("venue", new Venue());
        }

        return "venues/new";
    }
    
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String createVenue(@RequestBody @Valid @ModelAttribute Venue venue, BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
        if (errors.hasErrors()) {
            model.addAttribute("venue", venue);
            return "venues/new";
        }

        venueService.save(venue);
        redirectAttrs.addFlashAttribute("ok_message", "Added new venue");
        
        return "redirect:/venues";
    }
	@RequestMapping(value = "/update/{id}", method = RequestMethod.GET)
	public String updateVenuePage(@PathVariable("id") long id, Model model){
		Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
		model.addAttribute("venue", venue);
		return "venues/update/update";
	}
    
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String updateVenue(@PathVariable("id") long id, @RequestBody @Valid @ModelAttribute Venue venue, BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
        if (errors.hasErrors()) {
            model.addAttribute("venue", venue);
            return "venues/update/update";
        }
        
		Venue venueToUpdate = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));

        venueToUpdate.setName(venue.getName());
        venueToUpdate.setRoad(venue.getRoad());
        venueToUpdate.setPostcode(venue.getPostcode());
        venueToUpdate.setCapacity(venue.getCapacity());
		venueService.update(venueToUpdate);
        redirectAttrs.addFlashAttribute("ok_message", "Venue updated");
        
        return "redirect:/venues";
    }
}
