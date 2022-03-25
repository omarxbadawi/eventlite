package uk.ac.man.cs.eventlite.entities;

import java.io.IOException;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Response;

@Entity
public class Venue {

	@Id
	@GeneratedValue
	private long id;
	
	@NotEmpty(message = "Venue must have a name.")
	@Size(max = 255, message="The name must be less than 256 characters.")
	private String name;
	
	@NotEmpty(message = "Venue must have an address.")
	@Size(max = 299, message="The address must be less than 300 characters.")
	private String road;
	
	@NotEmpty(message = "Venue must have a postcode.")
	@Size(max = 10, message = "Postcode must be less than 10 characters.")
	private String postcode;
	
	@NotNull(message = "Venue must have a capacity.")
	@Min(value = 1, message = "Venue capacity must be positive.")
	private int capacity;
	
	private double longitude;
	
	private double latitude;
	
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
	
	public double getLongitude() {
		return longitude;
	};
	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLongLat() {
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken("pk.eyJ1IjoiYnNtaXRoMTU2IiwiYSI6ImNsMTU2emQ5MzB4cnIzanMwdTA5cDBmMW0ifQ.60o5nY2oKBQLMipUtSjuwQ")
				.query(road + " " + postcode).build();
		
		try {
			Response<GeocodingResponse> response = mapboxGeocoding.executeCall();
			Thread.sleep(1000L);
			
			GeocodingResponse geoResponse = response.body();
			Point coords = geoResponse.features().get(0).center();
			
			longitude = coords.longitude();
			latitude = coords.latitude();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		};
	}
}
