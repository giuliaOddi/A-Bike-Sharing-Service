package project.iot2023.station_resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

import project.iot2023.bike.Bike;

import com.google.gson.Gson; 

// This COAP resource return the list of available bikes
public class AvailableBikeResource extends CoapResource {
	
	private List<Bike> stationBikes; // All the bikes of the docking station

	public AvailableBikeResource(String name, List<Bike> sb) {
		super(name);
		this.stationBikes = sb;
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {		
		// Filter only available bikes		
		List<Bike> availableBike = new ArrayList<Bike>();
		for (Bike b : this.stationBikes) {
			if (b.getAvailable()){
				availableBike.add(b);
			}
		}
		/// Sending the list as a response (JSON format)
		if(availableBike.size() > 0) {
			String jsonBikes = new Gson().toJson(availableBike);
			exchange.respond(ResponseCode.CONTENT, jsonBikes, MediaTypeRegistry.APPLICATION_JSON);	// Successful response
		}
		// No bikes available
		else {
			exchange.respond(ResponseCode.NOT_FOUND);	// Unsuccessful response
		}
	}
	
}
