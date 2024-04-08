package project.iot2023.bike_resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.Gson;

// This COAP resource allows the cyclist to change the gear from the handlebar
public class GearboxResource extends CoapResource {

	private int gear = 4;	// Current gear

	public GearboxResource(String name) {
		super(name);
	}
	
	public void setGear(int g) {
		this.gear = g;
	}
	
	public int getGear() {
		return this.gear;
	}
	
	@Override
	public void handlePOST(CoapExchange exchange) {
		String request = new Gson().fromJson(exchange.getRequestText(), String.class);	// Request from cyclist
		// Increase the gear 
		if(request.equals("+") && getGear() != 8) {
			setGear(getGear() + 1);
			this.changed(); // For OBServation of the gear changes
			String jsonBikes = new Gson().toJson(getGear());
			exchange.respond(ResponseCode.CHANGED, jsonBikes, MediaTypeRegistry.APPLICATION_JSON);	// Successful response
		}
		// If 8th gear is insert, you can't increase it
		else if(request.equals("+") && getGear() == 8) {
			exchange.respond(ResponseCode.FORBIDDEN); // Operation forbidden 
		}
		// Decrease the gear
		else if(request.equals("-") && getGear() != 1) {
			setGear(getGear() - 1);
			this.changed(); // For OBServation of the gear changes
			String jsonBikes = new Gson().toJson(getGear());
			exchange.respond(ResponseCode.CHANGED, jsonBikes, MediaTypeRegistry.APPLICATION_JSON); // Successful response
		}
		// If 1st gear is insert, you can't decrease it
		else if(request.equals("-") && getGear() == 1) {
			exchange.respond(ResponseCode.FORBIDDEN); // Operation forbidden 
		}
		// Not a valid input 
		else {	
			exchange.respond(ResponseCode.BAD_REQUEST); // Unsuccessful response
		}
		
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {
		// Sending to the cyclist the current status of the gear
		String jsonBikes = new Gson().toJson(getGear());
		exchange.respond(ResponseCode.CONTENT, jsonBikes, MediaTypeRegistry.APPLICATION_JSON); // Return to the cyclist the current selected gear
	}
	
}
