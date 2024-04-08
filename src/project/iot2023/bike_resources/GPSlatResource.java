package project.iot2023.bike_resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.Gson;

import project.iot2023.bike.Bike;

// This COAP resource returns to the cyclist the current GPS latitude of the bike
public class GPSlatResource extends CoapResource {
	
	private Bike bike;

	public GPSlatResource(String name, Bike bike) {
		super(name);
		this.bike = bike;
	}
		
	@Override
	public void handleGET(CoapExchange exchange) {
		// Return the current GPS latitude
		exchange.respond(ResponseCode.CONTENT, new Gson().toJson("Latitude:  " + bike.getLatitude()), MediaTypeRegistry.APPLICATION_JSON);	
	}

}
