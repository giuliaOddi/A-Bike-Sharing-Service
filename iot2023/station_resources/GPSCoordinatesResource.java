package project.iot2023.station_resources;

import java.util.List;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.Gson;

import project.iot2023.bike.Bike;

// This COAP resource manages the GPS information from the Bike server
public class GPSCoordinatesResource extends CoapResource {
	
	private List<Bike> stationBikes; // All the bikes in the docking station

	public GPSCoordinatesResource(String name, List<Bike> list) {
		super(name);
		this.stationBikes = list; 
	}
	
	@Override
	public void handlePOST(CoapExchange exchange) {
		Bike req = new Gson().fromJson(exchange.getRequestText(), Bike.class); // Request from bike server
		Boolean found = false;	// To check if the bike exists
		for (Bike b : this.stationBikes) { // Searching for the bike 
			if(b.getId().equals(req.getId())) { // Correct bike
				// Save latitude and longitude received from the bike server 
				b.setLatitude(req.getLatitude());
				b.setLongitude(req.getLongitude());
				found = true;
				exchange.respond(ResponseCode.CHANGED); // Successful response to the bike server 
			}
		}
		//Not correct bikeID
		if (!found) {
			exchange.respond(ResponseCode.NOT_FOUND); // Unsuccessful response 
		}		
	}
}
