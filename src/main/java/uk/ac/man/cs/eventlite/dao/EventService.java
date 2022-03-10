package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface EventService {

	public long count();

	Iterable<Event> findPrevious();
	Iterable<Event> findUpcoming();
	Iterable<Event> findAll();

	public void update(Event event);

	public Optional<Event> findById(long id);

	public Event save(Event event);

	void deleteById(long id);

	boolean existsById(long id);

	Iterable<Event> searchUpcoming(String query);
	Iterable<Event> searchPrevious(String query);

	Iterable<Event> findByNameContainingIgnoreCase(String query);
}
