package project.iot2023.client;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

import com.google.gson.Gson;

import project.iot2023.bike.Bike;

// Manager class that check the current state of bikes
public class Manager {
	
	// This method manages the manger's request for the list of available bikes 
	public void availableBike(CoapClient available_client) {
		// Send the GET request
		Request bikesRequest = new Request(Code.GET); 
		CoapResponse available_response = available_client.advanced(bikesRequest);
		System.out.println("\nAsking for available bikes...");
		
		// Docking station disconnected
		if(available_response == null) {
			System.err.println("Error: the docking station isn't connected...");
		}
		// Return the list of available bikes as JSON 
		else if (available_response.getCode() == ResponseCode.CONTENT){
			String response = available_response.getResponseText();
			List<Bike> availableBikes = Arrays.asList(new Gson().fromJson(response, Bike[].class));
			System.out.println("\nAvailable bikes from the doking station: ");
			// Print the list
			for (Bike b : availableBikes) {
				b.getInformation();
			}	
		}
		// No bikes available
		else if (available_response.getCode() == ResponseCode.NOT_FOUND){
			System.out.println("No available bikes. "); 
		}
	}

	// This method manages the manger's request for the list of available bikes 
	public void rentedBike(CoapClient rented_client) {
		Request bikesRequest = new Request(Code.GET); 
		CoapResponse rented_response = rented_client.advanced(bikesRequest);
		System.out.println("\nAsking for rented bikes...");
		
		// Bike server disconnected
		if(rented_response == null) {
			System.err.println("Error: the bike isn't connected...");
		}
		// Return the list of rented bikes as JSON 
		if (rented_response.getCode() == ResponseCode.CONTENT){
			String response = rented_response.getResponseText();
			List<Bike> rentedBikes = Arrays.asList(new Gson().fromJson(response, Bike[].class));
			System.out.println("\nRented bikes from the doking station: ");
			// Print the list
			for (Bike b : rentedBikes) {
				b.getInformation();
			}	
		}
		// No bikes rented
		else if (rented_response.getCode() == ResponseCode.NOT_FOUND){
			System.out.println("No rented bikes."); 
		}
	}
	
	// This method manages the manger's request of checking the identity of a cyclist 
	public void checkIdentity(String bike) {
		// Checking is the inserted ID is valid 
		if (bike.length() != 10 ||  !bike.contains("-") || bike.charAt(5) != '-') {
			System.err.println("Error: this bike doesn't exist...");
		}
		else {
			// The port of the bike server is equal to the last 4 number of the bike ID
			String[] parts = bike.split("-");
			String port = parts[1];
			
			//COAP client to rent a bike from the bike
			String uri = String.format("coap://127.0.0.1:%s/bike-rent-resource", port);
			CoapClient rentBike = new CoapClient(uri);
			// Send a GET request to the bike server to know the identity of the cyclist
			Request bikesRequest = new Request(Code.GET);
			CoapResponse identityCyclist = rentBike.advanced(bikesRequest);
			// Valid request
			if(identityCyclist == null) {
				System.err.println("\nError: the bike isn't connected...");
			}
			// Successful response 
			else if (identityCyclist.getCode() == ResponseCode.CONTENT) {
				Bike bike_response = new Gson().fromJson(identityCyclist.getResponseText(), Bike.class);
				System.out.println("RECEIVED] bike " + bike_response.getId() + " rented by " + bike_response.getFiscalCode());
			}
			// Received an unsuccessful response because the bike isn't rented by anyone 
			else if (identityCyclist.getCode() == ResponseCode.BAD_OPTION){
				System.err.println("\nError: this bike isn't rented by anyone!"); 
			}
			// Not a valid request -> this bike doesn't exist
			else {
				System.err.println("\nError: this bike doesn't exist!");
			}
		}
	}

	public static void main(String[] args) {
		Manager manager = new Manager(); 
		
		//COAP Client to get the list of available bikes 
		CoapClient avilable_client = new CoapClient("coap://127.0.0.1:5683/available-resource");
		//COAP Client to get the list of rented bikes 
		CoapClient rented_client = new CoapClient("coap://127.0.0.1:5683/rented-resource");
		
		System.out.println("-- Manager --"); 
		
		System.out.println("\nHello Manager! What do you want to do? "); 
		try (Scanner in = new Scanner(System.in)) {					
			Boolean end = false;
			while(!end) {
				System.out.println("\n-------------------------------------------------"); 
				System.out.println("A) Check the list of available bikes \nB) Check the list of rented bikes ID \nC) Check the identity of a cyclist by the bikeID \nE) Exit"); 
				System.out.println("-------------------------------------------------"); 
				String choice = in.nextLine(); 
				// Checking the list of available bikes
				if(choice.equals("A")) {			
					manager.availableBike(avilable_client); 
				}
				// Checking the list of rented bikes
				else if(choice.equals("B")) {	
					manager.rentedBike(rented_client);					
				}
				// Checking the identity of a cyclist by the bikeID
				else if(choice.equals("C")) {
					System.out.println("\nInsert the ID of the bike that you want to check ");
					String bike = in.nextLine();
					manager.checkIdentity(bike); 
				}
				// The manager chooses to exit from the application
				else if (choice.equals("E")) {
					System.out.println("End... bye!");
					end = true; 
				}
				// The manager inserts a not valid command
				else {
					System.err.println("\nError: command not found"); 
				}
			}
			System.exit(0);
		}
		
	}

}
