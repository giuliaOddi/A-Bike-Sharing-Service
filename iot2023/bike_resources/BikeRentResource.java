package project.iot2023.bike_resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.Gson;

import project.iot2023.bike.Bike;

// This COAP resource allows the cyclist to rent the bike
public class BikeRentResource extends CoapResource {
	
	private Bike bike;	// The bike to rent 

	public BikeRentResource(String name, Bike bike) {
		super(name);
		this.bike = bike;
	}
		
	@Override
	public void handlePOST(CoapExchange exchange) {
		Bike req = new Gson().fromJson(exchange.getRequestText(), Bike.class);	// Cyclist request
		// Rent the bike 
		if (bike.getFiscalCode() == null && bike.getAvailable()) {	// Check if the bike is available 
			bike.setFiscalCode(req.getFiscalCode());
			bike.setAvailable(false);
			exchange.respond(ResponseCode.CHANGED);	// Send a successful response
		}
		// Release the bike when the cyclist ends the trip
		else if (bike.getFiscalCode().equals(req.getFiscalCode()) && !bike.getAvailable()) { // Check if this is the correct bike
			bike.setFiscalCode(null);
			bike.setAvailable(true);
			exchange.respond(ResponseCode.CHANGED);	// Send a successful response
		}
		// Bike already rented
		else if (bike.getFiscalCode() != req.getFiscalCode() && !bike.getAvailable()){
			exchange.respond(ResponseCode.BAD_REQUEST);	// Send an unsuccessful response
		}
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {
		// Check if the bike is rented 
		if (bike.getFiscalCode() != null && !bike.getAvailable()) {
			exchange.respond(ResponseCode.CONTENT, new Gson().toJson(bike), MediaTypeRegistry.APPLICATION_JSON);	// Send a JSON response 
		}
		// Check if the bike is available 
		else if (bike.getFiscalCode() == null && bike.getAvailable()) {
			exchange.respond(ResponseCode.BAD_OPTION);	// Send an unsuccessful response because the bike isn't rented by anyone 
		}
	}

}
