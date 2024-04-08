package project.iot2023.server;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapServer;

import project.iot2023.bike.Bike;
import project.iot2023.station_resources.AvailableBikeResource;
import project.iot2023.station_resources.GPSCoordinatesResource;
import project.iot2023.station_resources.RentResource;
import project.iot2023.station_resources.RentedBikeResource;

// This class implements the docking station D as a COAP server
public class DockingStationServer extends CoapServer {
	
	private List<Bike> stationBikes; // List of the bikes in the docking station
	private static int n_bike = 10;  // Fixed number of bikes in the docking station 
	
	public DockingStationServer(List<Bike> bikeAvailable) {
		setStationBikes(bikeAvailable); 
	}

	public List<Bike> getStationBikes() {
		return this.stationBikes;
	}

	public void setStationBikes(List<Bike> stationBikes) {
		this.stationBikes = stationBikes;
	}
	
	// Initializing the bikes of the docking station 
	public static List<Bike> setInitialBikes(){
		List<Bike> bikeAvailable = new ArrayList<Bike>();
		int i = 0;
		while(i < n_bike) { // Fixed number of bikes in the docking station 
			int port = 5665 + i;	// Using a different port number for each bike 		
			String id = String.format("B000%s-%s", i, port);	// Creating an unique 10-characters ID
			if (id.length() != 10) {	// The ID must be of 10-characters
				id = String.format("B00%s-%s", i, port);
			}
			Bike bike = new Bike(id); // Creating the bike
			bikeAvailable.add(bike);
			i++;
		}
		return bikeAvailable; // Return the bike's list 
	}

	public static void main(String[] args) {
		
		// Initializing the server 
		DockingStationServer dockingServer = new DockingStationServer(setInitialBikes());
		
		// COAP resource to return the list of the currently available bikes
		AvailableBikeResource available_resource = new AvailableBikeResource("available-resource", dockingServer.getStationBikes());
		dockingServer.add(available_resource);
		
		// COAP resource to return the list of the bikes that are currently rented by cyclists
		RentedBikeResource rented_resource = new RentedBikeResource("rented-resource", dockingServer.getStationBikes());
		dockingServer.add(rented_resource);
		
		// COAP resource to allow to rent a bike
		RentResource rent_resource = new RentResource("rent-resource", dockingServer.getStationBikes());
		dockingServer.add(rent_resource);
		
		// COAP resource to return the GPS coordinates
		GPSCoordinatesResource GPSresource = new GPSCoordinatesResource("gps-resource", dockingServer.getStationBikes());
		dockingServer.add(GPSresource);
		
		// Starting the docking station server
		dockingServer.start(); 
		
		System.out.println("-- Docking Station Server -- \n"); 
		System.out.println("Initial available bikes: "); 
		// Just to print all the information about bikes in the docking station
		for (Bike b : dockingServer.getStationBikes()) {
			b.getInformation();
		}

	}

}
