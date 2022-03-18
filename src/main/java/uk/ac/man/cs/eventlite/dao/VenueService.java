package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService {

	long count();

	Iterable<Venue> findAll();

	void save(Venue venue);

	Optional<Venue> findById(long parseLong);

	void update(Venue venue);

	void deleteById(long id);

	boolean existsById(long id);

	Iterable<Venue> findByNameContainingIgnoreCase(String query);

}
