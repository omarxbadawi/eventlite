package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.repository.CrudRepository;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueRepository extends CrudRepository<Venue, Long> {

    Iterable<Venue>	findAllByOrderByNameAsc();
    Iterable<Venue> findByNameContainingIgnoreCase(String query);


}
