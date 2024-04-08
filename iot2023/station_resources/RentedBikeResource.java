package project.iot2023.station_resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.Gson;

import project.iot2023.bike.Bike;

// This COAP resource returns the list of rented bikes 
public class RentedBikeResource extends CoapResource {
	
	private List<Bike> stationBikes; // All the bikes of the docking station

	public RentedBikeResource(String name, List<Bike> list) {
		super(name);
		this.stationBikes = list; 
	}

	@Override
	public void handleGET(CoapExchange exchange) {		
		// Filter only rented bikes		
		List<Bike> rentedBike = new ArrayList<Bike>();
		for (Bike b : this.stationBikes) {
			if (!b.getAvailable()) {
				rentedBike.add(b);
			}
		}
		// Sending the list as a response (JSON format)
		if(rentedBike.size() > 0) {
			String jsonBikes = new Gson().toJson(rentedBike);
			exchange.respond(ResponseCode.CONTENT, jsonBikes, MediaTypeRegistry.APPLICATION_JSON); // Successful response 
		}
		// No bikes rented
		else {
			exchange.respond(ResponseCode.NOT_FOUND); // Unsuccessful response
		}
	}
	
}
