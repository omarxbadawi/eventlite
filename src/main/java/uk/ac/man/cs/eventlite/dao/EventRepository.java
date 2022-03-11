package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;

import java.time.LocalDate;
import java.time.LocalTime;

public interface EventRepository extends CrudRepository<Event, Long>{

	Iterable<Event>	findByDateLessThanOrderByDateDescNameAsc(LocalDate nowDate);
	Iterable<Event>	findByDateGreaterThanOrderByDateAscNameAsc(LocalDate nowDate);
	Iterable<Event>	findByDateEqualsAndTimeGreaterThanEqualOrderByNameAsc(LocalDate nowDate, LocalTime nowTime);
	Iterable<Event>	findByDateEqualsAndTimeLessThanEqualOrderByNameAsc(LocalDate nowDate, LocalTime nowTime);

	Iterable<Event>	findAllByOrderByDateAscTimeAsc();

	long count();
	Iterable<Event>	findByNameContainingIgnoreCaseAndDateLessThanOrderByDateDescNameAsc(String query, LocalDate nowDate);
	Iterable<Event>	findByNameContainingIgnoreCaseAndDateGreaterThanOrderByDateAscNameAsc(String query, LocalDate nowDate);
	Iterable<Event>	findByNameContainingIgnoreCaseAndDateEqualsAndTimeGreaterThanEqualOrderByNameAsc(String query, LocalDate nowDate, LocalTime nowTime);
	Iterable<Event>	findByNameContainingIgnoreCaseAndDateEqualsAndTimeLessThanEqualOrderByNameAsc(String query, LocalDate nowDate, LocalTime nowTime);
	Iterable<Event> findByNameContainingIgnoreCase(String query);
}