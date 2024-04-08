package project.iot2023.bike_resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.Gson;

import project.iot2023.bike.Bike;

//This COAP resource allows the cyclist to press or release the rear brake
public class RearBrakeResource extends CoapResource {
	
	private Bike bike;	// Cyclist's bike

	public RearBrakeResource(String name, Bike bike) {
		super(name);
		this.bike = bike; 
	}
		
	@Override
	public void handlePOST(CoapExchange exchange) {
		String req = new Gson().fromJson(exchange.getRequestText(), String.class); // Request from the cyclist
		// The cyclist press the brake 
		if (req.equals("PRESS") && !bike.getRearPress()) { // Only if the rear brake isn't already pressed
			bike.setRearPress(true);
			this.changed(); // For OBServation of changes caused by the cyclist's pressure
			String jsonBikes = new Gson().toJson(bike.getRearPress());
			exchange.respond(ResponseCode.CHANGED, jsonBikes, MediaTypeRegistry.APPLICATION_JSON); // Successful response 
		}
		// The cyclist release the brake
		else if (req.equals("RELEASE") && bike.getRearPress()) { // Only if the front brake isn't already released
			bike.setRearPress(false);
			this.changed();  // For OBServation of changes caused by the cyclist's release
			String jsonBikes = new Gson().toJson(bike.getRearPress());
			exchange.respond(ResponseCode.CHANGED, jsonBikes, MediaTypeRegistry.APPLICATION_JSON); // Successful response 
		}
		// Not a valid input 
		else {
			exchange.respond(ResponseCode.BAD_REQUEST); // Unsuccessful response 
		}
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {
		// Sending to the cyclist the current status of the rear brake
		String jsonBikes = new Gson().toJson(bike.getRearPress());
		exchange.respond(ResponseCode.CONTENT, jsonBikes, MediaTypeRegistry.APPLICATION_JSON); // Return the current status of the rear brake 
	}

}
