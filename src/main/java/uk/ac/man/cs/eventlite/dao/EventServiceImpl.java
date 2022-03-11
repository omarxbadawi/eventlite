package uk.ac.man.cs.eventlite.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.eventlite.entities.Event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public Iterable<Event> findPrevious() {
		ArrayList<Event> previousEvents = new ArrayList<>();
		eventRepository.findByDateEqualsAndTimeLessThanEqualOrderByNameAsc(LocalDate.now(), LocalTime.now()).forEach(previousEvents::add);
		eventRepository.findByDateLessThanOrderByDateDescNameAsc(LocalDate.now()).forEach(previousEvents::add);
		return previousEvents;
	}

	@Override
	public Iterable<Event> findUpcoming() {
		ArrayList<Event> upcomingEvents = new ArrayList<>();
		eventRepository.findByDateEqualsAndTimeGreaterThanEqualOrderByNameAsc(LocalDate.now(), LocalTime.now()).forEach(upcomingEvents::add);
		eventRepository.findByDateGreaterThanOrderByDateAscNameAsc(LocalDate.now()).forEach(upcomingEvents::add);
		return upcomingEvents;
	}


	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAllByOrderByDateAscTimeAsc();
	}

	@Override
	public Optional<Event> findById(long id) {
		return eventRepository.findById(id);
	}

	public Iterable<Event> findByNameContainingIgnoreCase(String query) {
		return eventRepository.findByNameContainingIgnoreCase(query);
	}

	public Iterable<Event> searchUpcoming(String query) {
		ArrayList<Event> upcomingSearchedEvents = new ArrayList<>();
		eventRepository.findByNameContainingIgnoreCaseAndDateEqualsAndTimeGreaterThanEqualOrderByNameAsc(query, LocalDate.now(), LocalTime.now()).forEach(upcomingSearchedEvents::add);
		eventRepository.findByNameContainingIgnoreCaseAndDateGreaterThanOrderByDateAscNameAsc(query, LocalDate.now()).forEach(upcomingSearchedEvents::add);
		return upcomingSearchedEvents;
	}

	public Iterable<Event> searchPrevious(String query) {
		ArrayList<Event> previousSearchedEvents = new ArrayList<>();
		eventRepository.findByNameContainingIgnoreCaseAndDateEqualsAndTimeLessThanEqualOrderByNameAsc(query, LocalDate.now(), LocalTime.now()).forEach(previousSearchedEvents::add);
		eventRepository.findByNameContainingIgnoreCaseAndDateLessThanOrderByDateDescNameAsc(query, LocalDate.now()).forEach(previousSearchedEvents::add);
		return previousSearchedEvents;
	}
	
	@Override
	public Event save(Event event) {
		return eventRepository.save(event);
	}

	@Override
	public void update(Event event) {
		eventRepository.save(event);
	} 
	
	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}
	
	@Override
	public boolean existsById(long id) {
		return eventRepository.existsById(id);
	}
}
