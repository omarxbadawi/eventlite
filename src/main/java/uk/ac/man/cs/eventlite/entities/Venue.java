package uk.ac.man.cs.eventlite.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Venue {

	@Id
	@GeneratedValue
	private long id;
	
	@NotNull(message = "Venue must have a name.")
	@Size(max = 255, message="The name must be less than 256 characters.")
	private String name;
	
	@NotNull(message = "Venue must have an address.")
	@Size(max = 299, message="The address must be less than 300 characters.")
	private String road;
	
	@NotNull(message = "Venue must have a postcode.")
	@Size(max = 10, message = "Postcode must be less than 10 characters.")
	private String postcode;
	
	@NotNull(message = "Venue must have a capacity.")
	@Min(value = 1, message = "Venue capacity must be positive.")
	private int capacity;

	public Venue() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public String getRoad() {
		return road;
	}

	public void setRoad(String road) {
		this.road = road;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
}
