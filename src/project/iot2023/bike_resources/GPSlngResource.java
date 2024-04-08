package project.iot2023.bike_resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.Gson;

import project.iot2023.bike.Bike;

//This COAP resource returns to the cyclist the current GPS longitude of the bike
public class GPSlngResource extends CoapResource {
	
	private Bike bike;

	public GPSlngResource(String name, Bike bike) {
		super(name);
		this.bike = bike; 
	}
		
	@Override
	public void handleGET(CoapExchange exchange) {
		// Return the current GPS latitude
		exchange.respond(ResponseCode.CONTENT, new Gson().toJson("Longitude: " + bike.getLongitude()), MediaTypeRegistry.APPLICATION_JSON);
	}

}
