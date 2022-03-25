package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {

	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	
	@Autowired
	private VenueRepository venueRepository;

	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAllByOrderByNameAsc();
	}

	@Override
	public Venue save(Venue venue) {
		return venueRepository.save(venue);
	}
	
	@Override
	public Optional<Venue> findById(long venue) {
		return venueRepository.findById(venue);
	}

	@Override
	public void update(Venue venue) {
		venueRepository.save(venue);
	}

	@Override
	public void deleteById(long id) {
		venueRepository.deleteById(id);
	}

	@Override
	public boolean existsById(long id) {
		return venueRepository.existsById(id);
	}

	public Iterable<Venue> findByNameContainingIgnoreCase(String query) {
		return venueRepository.findByNameContainingIgnoreCase(query);
	}

}
