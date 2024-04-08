package project.iot2023.station_resources;

import java.util.List;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.Gson;

import project.iot2023.bike.Bike;

// This COAP resource allows a cyclist to rent a bike
public class RentResource extends CoapResource {
	
	private List<Bike> stationBikes; // All the bikes of the docking station

	public RentResource(String name, List<Bike> sb) {
		super(name);
		this.stationBikes = sb;
	}
	
	@Override
	public void handlePOST(CoapExchange exchange) {
		Bike req = new Gson().fromJson(exchange.getRequestText(), Bike.class); // Request from the cyclist		
		Boolean found = false;	// To check if the bike exists 
		// Searching the bike
		for (Bike b : this.stationBikes) {
			if(b.getFiscalCode() != null) {
				// The client wants to finish the trip, release the bike 
				if(b.getFiscalCode().equals(req.getFiscalCode())) {
					b.setAvailable(true);
					b.setFiscalCode(null);
					exchange.respond(ResponseCode.CHANGED); // Ok: trip finished and resources released
				}
				// The client try to rent a not available bike
				else if(b.getId().equals(req.getId()) && !b.getAvailable()) {
					found = true; 
					exchange.respond(ResponseCode.BAD_REQUEST); // Unsuccessful response 
				}
			}
			// Rent the bike 
			else if(b.getId().equals(req.getId()) && b.getAvailable()) { // Check if the correct bike is available
				b.setAvailable(false);
				b.setFiscalCode(req.getFiscalCode());
				found = true;
				exchange.respond(ResponseCode.CHANGED); // Successful rented 
			}
		}
		// Not a correct bikeID
		if (!found) {
			exchange.respond(ResponseCode.NOT_FOUND); // Bike not found -> unsuccessful response
		}		
	}
}
