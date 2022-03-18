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
import java.util.*;

@Controller
@RequestMapping(value = "/", produces = { MediaType.TEXT_HTML_VALUE })
public class HomePageController {

    @Autowired
    private VenueService venueService;

    @Autowired
    private EventService eventService;

    @GetMapping
    public String getAllVenues(Model model) {

        HashMap<Venue, Integer> venueCounts = new HashMap<>();
        Iterable<Venue> allVenues = venueService.findAll();
        allVenues.forEach(venue -> venueCounts.put(venue, 0));

        Iterable<Event> events = eventService.findAll();
        events.forEach(event -> venueCounts.put(event.getVenue(), venueCounts.get(event.getVenue()) + 1));

        List<Map.Entry<Venue, Integer>> sortedVenues = new ArrayList<>(venueCounts.entrySet());
        sortedVenues.sort(Map.Entry.comparingByValue());
        List<Venue> topThreeVenues = new ArrayList<>();
        for (int i = 1; i <= Math.min(3, sortedVenues.size()); i++) {
            topThreeVenues.add(sortedVenues.get(sortedVenues.size() - i).getKey());
        }

        
        model.addAttribute("venues", topThreeVenues);
        Iterable<Event> upcommingEvents = eventService.findUpcoming();
        List<Event> upcomingThreeEvents = new ArrayList<Event>();
        int i = 0;
        for( Event e : upcommingEvents ){
        	if (i > 2) {
        		break;
        	}   
        	upcomingThreeEvents.add(e);
        	i++;
        }
        model.addAttribute("events", upcomingThreeEvents);

        return "/index";
    }


}
