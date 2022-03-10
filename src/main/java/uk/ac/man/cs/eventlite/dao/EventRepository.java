package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;

import java.time.LocalDate;
import java.time.LocalTime;

public interface EventRepository extends CrudRepository<Event, Long>{

	Iterable<Event>	findByDateLessThanEqualAndTimeLessThanOrderByDateDescNameAsc(LocalDate date, LocalTime time);
	Iterable<Event>	findByDateGreaterThanEqualAndTimeGreaterThanEqualOrderByDateAscNameAsc(LocalDate date, LocalTime time);

	Iterable<Event>	findAllByOrderByDateAscTimeAsc();

	long count();

	Iterable<Event>	findByNameContainingIgnoreCaseAndDateLessThanEqualAndTimeLessThanOrderByDateDescNameAsc(String query, LocalDate date, LocalTime time);
	Iterable<Event>	findByNameContainingIgnoreCaseAndDateGreaterThanEqualAndTimeGreaterThanEqualOrderByDateAscNameAsc(String query, LocalDate date, LocalTime time);
	Iterable<Event> findByNameContainingIgnoreCase(String query);
}