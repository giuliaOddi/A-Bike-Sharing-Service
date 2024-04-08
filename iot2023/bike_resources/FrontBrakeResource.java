package project.iot2023.bike_resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.Gson;

import project.iot2023.bike.Bike;

// This COAP resource allows the cyclist to press or release the front brake
public class FrontBrakeResource extends CoapResource {
	
	private Bike bike;	// Cyclist's bike

	public FrontBrakeResource(String name, Bike bike) {
		super(name);
		this.bike = bike; 
	}
		
	@Override
	public void handlePOST(CoapExchange exchange) {
		String req = new Gson().fromJson(exchange.getRequestText(), String.class);	// Request from the cyclist
		// The cyclist presses the brake 
		if (req.equals("PRESS") && !bike.getFrontPress()) {	// Only if the front brake isn't already pressed
			bike.setFrontPress(true);
			this.changed();	// For OBServation of changes caused by the cyclist's pressure
			String jsonBikes = new Gson().toJson(bike.getFrontPress());
			exchange.respond(ResponseCode.CHANGED, jsonBikes, MediaTypeRegistry.APPLICATION_JSON);	// Successful response 
		}
		// The cyclist releases the brake 
		else if (req.equals("RELEASE") && bike.getFrontPress()) { // Only if the front brake isn't already released
			bike.setFrontPress(false);
			this.changed(); // For OBServation of changes caused by the cyclist's release
			String jsonBikes = new Gson().toJson(bike.getFrontPress());
			exchange.respond(ResponseCode.CHANGED, jsonBikes, MediaTypeRegistry.APPLICATION_JSON); // Successful response 
		}
		// Not a valid input 
		else {
			exchange.respond(ResponseCode.BAD_REQUEST);	// Unsuccessful response
		}
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {
		// Sending to the cyclist the current status of the front brake
		String jsonBikes = new Gson().toJson(bike.getFrontPress());
		exchange.respond(ResponseCode.CONTENT, jsonBikes, MediaTypeRegistry.APPLICATION_JSON);	// Return the current status of the front brake 
	}

}
