package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService {

	long count();

	Iterable<Venue> findAll();

	void save(Venue venue);
}
