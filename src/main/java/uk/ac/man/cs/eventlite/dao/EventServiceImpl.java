package uk.ac.man.cs.eventlite.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.eventlite.entities.Event;

import java.time.LocalDate;
import java.time.LocalTime;
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
		return eventRepository.findByDateLessThanEqualAndTimeLessThanOrderByDateDescNameAsc(LocalDate.now(), LocalTime.now());
	}

	@Override
	public Iterable<Event> findUpcoming() {
		return eventRepository.findByDateGreaterThanEqualAndTimeGreaterThanEqualOrderByDateAscNameAsc(LocalDate.now(), LocalTime.now());
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
		return eventRepository.findByNameContainingIgnoreCaseAndDateGreaterThanEqualAndTimeGreaterThanEqualOrderByDateAscNameAsc(query, LocalDate.now(), LocalTime.now());
	}

	public Iterable<Event> searchPrevious(String query) {
		return eventRepository.findByNameContainingIgnoreCaseAndDateLessThanEqualAndTimeLessThanOrderByDateDescNameAsc(query, LocalDate.now(), LocalTime.now());
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
