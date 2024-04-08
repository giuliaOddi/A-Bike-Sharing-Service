package project.iot2023.client;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;

import com.google.gson.Gson;

import project.iot2023.bike.Bike;

import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class CyclistClient {
	
	// The cyclist is identifier by his/her 16-character fiscal code
	private static String fiscalCode; 
	
	// Get and set the fiscal code
	public static String getFiscalCode() {
		return fiscalCode; 
	}
	
	public static void setFiscalCode(String fc) {
		fiscalCode = fc;
	}

	public static void main(String[] args) throws InterruptedException {
		
		//COAP client to get the list of available bikes 
		CoapClient available_client = new CoapClient("coap://127.0.0.1:5683/available-resource");
		System.out.println("-- Cyclist --"); 
		
		//COAP client to rent a bike from the docking station
		CoapClient rentResource_client = new CoapClient("coap://127.0.0.1:5683/rent-resource");
		
		System.out.println("\nPlease enter your fiscal code: "); 
		try (Scanner in = new Scanner(System.in)) {
			//Checking if the fiscal code is valid
			Boolean validFiscalCode = false;
			while(!validFiscalCode) {
				// Set the fiscal code of the client 
				String fiscal_code = in.nextLine(); 
				// Checking if the fiscal code is correct
				if (fiscal_code.matches( "^([A-Z]{6}[0-9]{2}[ABCDEHLMPRST]{1}[0-9]{2}[A-Z]{1}[0-9]{3}[A-Z]{1})$")) {
					setFiscalCode(fiscal_code); 	
					validFiscalCode = true;
				}
				// Not a valid fiscal code -> retry
				else{
					System.err.println("\nError: enter a valid fiscal code: "); 
				}
			}			
			System.out.println("Hello cyclist " + getFiscalCode()); 			
			
			// Starting the interaction with the cyclist
			Boolean end = false;
			while(!end) {
				System.out.println("\n-------------------------------------"); 
				System.out.println("Choose an action: \nA) Check the list of available bikes \nB) Rent a bike by its ID \nE) Exit"); 
				System.out.println("-------------------------------------"); 
				String choice = in.nextLine(); 
				// The cyclist chooses to check the list of available bikes
				if(choice.equals("A")) {	
					// The cyclist asks for the list of available bikes 
					Request bikesRequest = new Request(Code.GET); 
					CoapResponse available_response = available_client.advanced(bikesRequest);
					System.out.println("\nAsking for available bikes...");
					
					// Docking station disconnected
					if(available_response == null) {
						System.err.println("\nError: the docking station isn't connected...");
					}
					// Return the list of available bikes as JSON 
					else if (available_response.getCode() == ResponseCode.CONTENT){
						String response = available_response.getResponseText();
						List<Bike> availableBikes = Arrays.asList(new Gson().fromJson(response, Bike[].class));
						System.out.println("\nAvailable bikes from the doking station: ");
						// Print the bike's list 
						for (Bike b : availableBikes) {
							b.getInformation();
						}	
					}
					// No bikes available
					else if (available_response.getCode() == ResponseCode.NOT_FOUND){
						System.out.println("No available bikes. ");
					}
				}
				// The cyclist chooses to rent a bike
				else if (choice.equals("B")){
					// The cyclist chooses a bike
					System.out.println("\nInsert the ID of the bike that you want to rent: ");
					String bikeID = in.nextLine();	
					
					// Error if the ID isn't valid 
					if (!bikeID.contains("-") || bikeID.length() != 10 || bikeID.charAt(5) != '-'){
						System.err.println("\nError: this bike doesn't exist...");
					}
					// Valid ID
					else {
						// Sending a JSON request with the bikeID and the fiscal code
						Bike bike = new Bike(bikeID, fiscalCode);
						String jsonBikes = new Gson().toJson(bike);
						CoapResponse rentResource_response = rentResource_client.post(jsonBikes, MediaTypeRegistry.APPLICATION_JSON); 
						
						// The port of the bike server is equal to the last 4 number of the bike ID
						String[] parts = bikeID.split("-");
						String port = parts[1];
						
						// Creation of all the necessary COAP clients:
						
						//COAP client to rent a bike from the bike 
						String uri = String.format("coap://127.0.0.1:%s/bike-rent-resource", port);
						CoapClient rent_client = new CoapClient(uri);
						
						// Observable COAP client for front brake
						uri = String.format("coap://127.0.0.1:%s/front-brake-resource", port);
						CoapClient frontClient = new CoapClient(uri);		
						CoapHandler frontHandler = new CoapHandler() {			
							@Override
							public void onLoad(CoapResponse response) {
								System.out.println("RECEIVED] Front brake: " + response.getResponseText());
							}
							@Override
							public void onError() {
								System.err.println("Error on onError method");				
							}
						}; 
						
						// Observable COAP client for rear brake 
						uri = String.format("coap://127.0.0.1:%s/rear-brake-resource", port);
						CoapClient rearClient = new CoapClient(uri);		
						CoapHandler rearHandler = new CoapHandler() {			
							@Override
							public void onLoad(CoapResponse response) {
								System.out.println("RECEIVED] Rear brake: " + response.getResponseText());
							}
							@Override
							public void onError() {
								System.err.println("Error on onError method");				
							}
						};
						
						// Observable COAP client for the gearbox
						uri = String.format("coap://127.0.0.1:%s/gearbox-resource", port);
						CoapClient gearClient = new CoapClient(uri);		
						CoapHandler gearHandler = new CoapHandler() {			
							@Override
							public void onLoad(CoapResponse response) {
								System.out.println("RECEIVED] Gearbox: " + response.getResponseText());
							}
	
							@Override
							public void onError() {
								System.err.println("Error on onError method");				
							}
						};
	
						// COAP client to get the GPS latitude
						uri = String.format("coap://127.0.0.1:%s/gps-lat-resource", port);
						CoapClient gpsLatClient = new CoapClient(uri);
						CoapHandler gpsLatHandler = new CoapHandler() {			
							@Override
							public void onLoad(CoapResponse response) {
								System.out.println("RECEIVED] GPS " + new Gson().fromJson(response.getResponseText(), String.class));
							}
	
							@Override
							public void onError() {
								System.err.println("Error on onError method");				
							}
						};
						
						// COAP client to get the GPS latitude
						uri = String.format("coap://127.0.0.1:%s/gps-lng-resource", port);
						CoapClient gpsLngClient = new CoapClient(uri);
						CoapHandler gpsLngHandler = new CoapHandler() {			
							@Override
							public void onLoad(CoapResponse response) {
								System.out.println("RECEIVED] GPS " + new Gson().fromJson(response.getResponseText(), String.class));
							}
	
							@Override
							public void onError() {
								System.err.println("Error on onError method");				
							}
						};

						// Successful response from the docking station
						if(rentResource_response.getCode() == ResponseCode.CHANGED) {
							System.out.println("OK: successful request to the docking station!");
							
							// Second request to rent the bike -> to the bike server 
							System.out.println("\nPost to the bike server...");
							CoapResponse rent_response = rent_client.post(jsonBikes, MediaTypeRegistry.APPLICATION_JSON); 
							
							// Bike server disconnected
							if(rent_response == null) {
								System.err.println("\nError: the bike isn't connected...");
							}
							// Successful response from the bike server
							else if (rent_response.getCode() == ResponseCode.CHANGED) {
								System.out.println("OK: successful request to the bike!");
								
								System.out.println("\nNow you can use the bike: " + bikeID + "\n");
								// Starting the observation 
								frontClient.observe(frontHandler); 
								rearClient.observe(rearHandler); 
								gearClient.observe(gearHandler);
								gpsLatClient.observe(gpsLatHandler);  							 
								gpsLngClient.observe(gpsLngHandler); 
									
								Boolean finished = false;
								Thread.sleep(200); // Only to have a pretty system.out (first observation - second the possibilities)
								
								// Until the cyclist decides to stop the trip 
								while(!finished) {
									// Giving to cyclist all the possibilities
									System.out.println("\n-----------------------------------------------------------------"); 
									System.out.println("SELECT AN ACTION:"); 
									System.out.println("-> '1' to press the front brake, '2' to release the front brake "); 
									System.out.println("-> '3' to press the rear brake,  '4' to release the rear brake "); 
									System.out.println("-> '+' to increse the gear, 	 '-' for decrease the gear"); 
									System.out.println("-> 'B' to know the current status of brakes");
									System.out.println("-> 'G' to know the current selected gear");
									System.out.println("-> 'GPS' to know where you are "); 
									System.out.println("-> 'q' for finish the trip"); 
									System.out.println("-----------------------------------------------------------------"); 
									String command = in.nextLine(); 
									
									// The cyclist wants to press the front brake -> send a POST request with JSON string
									if (command.equals("1")) {
										System.out.println("\nYou pressed the front brake...");
										frontClient.post(new Gson().toJson("PRESS"), MediaTypeRegistry.APPLICATION_JSON); 
									}
									// The cyclist wants to release the front brake -> send a POST request with JSON string
									else if (command.equals("2")) {
										System.out.println("\nYou released the front brake..."); 
										frontClient.post(new Gson().toJson("RELEASE"), MediaTypeRegistry.APPLICATION_JSON); 
									}
									// The cyclist wants to press the rear brake -> send a POST request with JSON string
									else if (command.equals("3")) {
										System.out.println("\nYou pressed the rear brake..."); 
										rearClient.post(new Gson().toJson("PRESS"), MediaTypeRegistry.APPLICATION_JSON); 
									}
									// The cyclist wants to release the rear brake -> send a POST request with JSON string
									else if (command.equals("4")) {
										System.out.println("\nYou released the rear brake..."); 
										rearClient.post(new Gson().toJson("RELEASE"), MediaTypeRegistry.APPLICATION_JSON); 
									}
									// The cyclist wants to increase the gear -> send a POST request with JSON string
									else if(command.equals("+")) {
										System.out.println("\nYou want to increase the gear..."); 
										CoapResponse gear_response = gearClient.post(new Gson().toJson(command), MediaTypeRegistry.APPLICATION_JSON); 
										if (gear_response.getCode() == ResponseCode.FORBIDDEN) {
											System.err.println("\nError: operation not allowed!!!"); 
										}
									}
									// The cyclist wants to decrease the gear -> send a POST request with JSON string
									else if(command.equals("-")) {
										System.out.println("\nYou want to decrease the gear..."); 
										CoapResponse gear_response = gearClient.post(new Gson().toJson(command), MediaTypeRegistry.APPLICATION_JSON); 
										if (gear_response.getCode() == ResponseCode.FORBIDDEN) {
											System.err.println("\nError: operation not allowed!!!"); 
										}
									}
									// The cyclist wants to know the current status of brakes -> send a GET request
									else if(command.equals("B")) {
										Request front_request = new Request(Code.GET); 
										CoapResponse front_response = frontClient.advanced(front_request);	// Request for front brake
										Request rear_request = new Request(Code.GET); 
										CoapResponse rear_response = rearClient.advanced(rear_request);		// request for rear brake 
										System.out.println("\nBRAKES STATUS: front " + front_response.getResponseText() + " - rear " + rear_response.getResponseText());
									}
									// The cyclist wants to know the current state of the gear -> send a GET request
									else if(command.equals("G")) {
										Request gear_request = new Request(Code.GET); 
										CoapResponse gear_response = gearClient.advanced(gear_request);
										System.out.println("\nCURRENT GEARBOX: " + gear_response.getResponseText());
									}
									// The cyclist wants to know the GPS coordinates -> send a GET request
									else if(command.equals("GPS")) {
										Request lat_request = new Request(Code.GET); 
										CoapResponse gpsLat_response = gpsLatClient.advanced(lat_request);	// Request for latitude
										Request lng_request = new Request(Code.GET); 
										CoapResponse gpsLng_response = gpsLngClient.advanced(lng_request);		// Request for longitude
										System.out.println("\nGPS: " + new Gson().fromJson(gpsLat_response.getResponseText(), String.class) + " " + new Gson().fromJson(gpsLng_response.getResponseText(), String.class));
									}
									// The cyclist wants to finish the trip 
									else if (command.equals("q")) {										
										// Releasing to the docking station
										CoapResponse finished_docking = rentResource_client.post(jsonBikes, MediaTypeRegistry.APPLICATION_JSON); 
										// Releasing to the bike 
										CoapResponse finished_bike = rent_client.post(jsonBikes, MediaTypeRegistry.APPLICATION_JSON); 
										// Successful requests
										if(finished_docking.getCode() == ResponseCode.CHANGED && finished_bike.getCode() == ResponseCode.CHANGED) {
											// Releasing the bike 
											System.out.println("\nBike returned to the docking station...");
											System.out.println("You've finished your trip!!!"); 
											finished = true;
											end = true;
										}
										// Not a successful response from the docking station
										else if(finished_docking.getCode() == ResponseCode.BAD_REQUEST) {
											System.err.println("\nError: something went wrong with your request to the docking station..."); 
										}
										// Not a successful response from the bike server
										else if(finished_bike.getCode() == ResponseCode.BAD_REQUEST) {
											System.out.println("\nError: something went wrong with your request to the bike server..."); 
										}
									}
									// Not a valid command
									else {
										System.err.println("\nError: command not found"); 
									}
								}
							}
							// Bike rented by someone else 
							else if(rent_response.getCode() == ResponseCode.BAD_REQUEST ) {	
								System.err.println("\nError: this bike is already rented!");
							}
						}
						// Bike rented by someone else 
						else if (rentResource_response.getCode() == ResponseCode.BAD_REQUEST) {
							System.err.println("\nError: this bike isn't available!");
						}
						// Not a valid bike ID 
						else if (rentResource_response.getCode() == ResponseCode.NOT_FOUND ) {
							System.err.println("\nError: this bike doesn't exist!"); 	
						}
					}
				}
				// The cyclist chooses to exit from the application
				else if (choice.equals("E")) {
					System.out.println("End... bye");
					end = true; // End the application
				}
				// Not a valid command
				else {
					System.err.println("\nError: command not found"); 
				}
			}
			System.exit(0);
		}
	}
	
}
