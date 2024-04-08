package project.iot2023.server;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

import com.google.gson.Gson;

import project.iot2023.bike.Bike;
import project.iot2023.bike_resources.GPSlatResource;
import project.iot2023.bike_resources.GPSlngResource;
import project.iot2023.bike_resources.BikeRentResource;
import project.iot2023.bike_resources.FrontBrakeResource;
import project.iot2023.bike_resources.GearboxResource;
import project.iot2023.bike_resources.RearBrakeResource;

// This class implements the bike CPU that controls a bike
public class BikeEServer extends CoapServer {
	
	private Bike bike;				// The bike controlled by this bike server
	private static int nBike = 10; 	// Fixed number of bikes in the docking station 
	
	public double max = 0.001;			// Range of changes for GPS coordinates
	public double round = 10000000.0; 	// Used to avoid too much decimal numbers
	
	public BikeEServer(int... ports) {
		super(ports);
	}
	
	public Bike getBike() {
		return this.bike;
	}
	public void setBike(Bike b) {
		this.bike = b; 
	}
	
	// This method manages the creation of new GPS coordinates
	public void setGPS(BikeEServer bikeCPU, GPSlatResource gpsLatBike, GPSlngResource gpsLngBike, CoapClient gpsClient) {
		// Generating random but meaningful GPS latitude every 20 seconds 
    	Random random = new Random();
    	double generateLat = -max + (max - (-max)) * random.nextDouble();	// Add or remove 0.001 from latitude every 20 seconds
    	double latitude = Math.round(( bikeCPU.getBike().getLatitude() + generateLat) * round) / round;	// To avoid too much decimal numbers
    	bikeCPU.getBike().setLatitude(latitude); 
    	gpsLatBike.changed(); // For OBServation of changes caused by the new latitude

    	// Generating random but meaningful GPS longitude every 20 seconds 
    	double generateLong =  -max + (max - (-max)) * random.nextDouble();	// Add or remove 0.001 from longitude every 20 seconds
    	double longitude = Math.round((bikeCPU.getBike().getLongitude() + generateLong) * round) / round;	// To avoid too much decimal numbers
    	bikeCPU.getBike().setLongitude(longitude); 
    	gpsLngBike.changed(); // For OBServation of changes caused by the new longitude

    	// Sending a POST to the docking station	
    	String jsonBikes = new Gson().toJson(bikeCPU.getBike());
    	CoapResponse gps_response = gpsClient.post(jsonBikes, MediaTypeRegistry.APPLICATION_JSON); 
    	if (gps_response.getCode() == ResponseCode.CHANGED) {
    		System.out.println("GPS coordinates updated with success!");
    	}
    	else if (gps_response.getCode() == ResponseCode.NOT_FOUND) {
    		System.err.println("Error: GPS coordinates not update!");
    	}
	}
	
	public static void main(String[] args) {
		
		// Generating nBike server bike
		int i = 0;
		while(i < nBike) {

			int port = 5665 + i;	// Using a different port number for each bike 
			final BikeEServer bikeCPU = new BikeEServer(port); 
			// Creating the unique id and the bike 
			String id = String.format("B000%s-%s", i, port);
			if (id.length() != 10) { // The ID must be of 10-characters
				id = String.format("B00%s-%s", i, port);
			}
			bikeCPU.setBike(new Bike(id));
			
			// COAP resource that allow a cyclist to rent the bike
			BikeRentResource bikeRent_resource = new BikeRentResource("bike-rent-resource", bikeCPU.getBike());
			bikeCPU.add(bikeRent_resource);
			
			// COAP resource (OBS) that allow the cyclist to press and release the front brake
			FrontBrakeResource frontBrake_resource = new FrontBrakeResource("front-brake-resource", bikeCPU.getBike());
			frontBrake_resource.setObservable(true);
			frontBrake_resource.getAttributes().setObservable();
			bikeCPU.add(frontBrake_resource); 
			
			// COAP resource (OBS) that allow the cyclist to press and release the rear brake
			RearBrakeResource rearBrake_resource = new RearBrakeResource("rear-brake-resource", bikeCPU.getBike());
			rearBrake_resource.setObservable(true);
			rearBrake_resource.getAttributes().setObservable();
			bikeCPU.add(rearBrake_resource); 
			
			// COAP resource (OBS) that allow the cyclist to change the gear 
			GearboxResource gearbox_resource = new GearboxResource("gearbox-resource");
			gearbox_resource.setObservable(true);
			gearbox_resource.getAttributes().setObservable();
			bikeCPU.add(gearbox_resource); 			

			// COAP resource (OBS) that return the current GPS latitude of the bike
			final GPSlatResource gpsLat_resource = new GPSlatResource("gps-lat-resource", bikeCPU.getBike());
			gpsLat_resource.setObservable(true);
			gpsLat_resource.getAttributes().setObservable();
			bikeCPU.add(gpsLat_resource);
			
			// COAP resource (OBS) that return the current GPS longitude of the bike
			final GPSlngResource gpsLng_resource = new GPSlngResource("gps-lng-resource", bikeCPU.getBike());
			gpsLng_resource.setObservable(true);
			gpsLng_resource.getAttributes().setObservable();
			bikeCPU.add(gpsLng_resource);
			
			// Starting the bike server 
			bikeCPU.start(); 
			System.out.println("-- Bike E Server -- \n"); 
			
			// COAP Client to manage GPS coordinates
			final CoapClient gpsClient = new CoapClient("coap://127.0.0.1:5683/gps-resource");
				
			Timer t = new Timer();
			t.schedule(new TimerTask() {
			    @Override
			    public void run() {
			    	// Generating GPS coordinates only if the bike is rented by someone
			        if (bikeCPU.getBike().getFiscalCode() != null) { 
			        	bikeCPU.setGPS(bikeCPU, gpsLat_resource, gpsLng_resource, gpsClient); 
			        }
			    }
			}, 0, 20000);	// New coordinates every 20 seconds 

			i++;
		}			
	}
}
