package project.iot2023.bike;

// Bike class useful to manage all the necessary information
public class Bike{
	
	private String id;	// Unique 10-character identifier
	private String fiscalCode = null;	// Identifier of the cyclist who rented this bike 
	private Boolean available = true;	// Set all bikes to available at beginning
	// Campus GPS coordinates
	private double latitude = 44.7653076; 
	private double longitude = 10.3067761; 
	private Boolean frontBrakePress = false;	// Manage the front brake of the bike
	private Boolean rearBrakePress = false;		// Manage the rear brake of the bike
	private int gearbox = 4;	// Manage the gear box of the bike
	
	public Bike(String i) {
		this.id = i;
	}
	
	public Bike(String i, String fc) {
		this.id = i;
		this.fiscalCode = fc;
	}
	
	// Bike identifier
	public void setId(String id) {
		this.id = id; 
	}
	
	public String getId() {
		return this.id; 
	}
	
	// Cyclist identifier
	public void setFiscalCode(String fc) {
		this.fiscalCode = fc; 
	}
	
	public String getFiscalCode() {
		return this.fiscalCode; 
	}
	
	// Check and set the bike's availability 
	public void setAvailable(Boolean av) {
		this.available = av; 
	}
	
	public Boolean getAvailable() {
		return this.available; 
	}
	
	// Set and get the bike's latitude
	public void setLatitude(double lat) {	// Only used by BikeEServer to set the new GPS latitude 
		this.latitude = lat; 
	}
	
	public double getLatitude() {
		return this.latitude; 
	}
	
	// Set and get the bike's longitude
	public void setLongitude(double lng) {	// Only used by BikeEServer to set the new GPS latitude 
		this.longitude = lng; 
	}
	
	public double getLongitude() {
		return this.longitude; 
	}
	
	// Set and get the bike's front brake
	public void setFrontPress(Boolean f) {
		this.frontBrakePress = f; 
	}
	
	public Boolean getFrontPress() {
		return this.frontBrakePress; 
	}
	
	// Set and get the bike's rear brake
	public void setRearPress(Boolean r) {
		this.rearBrakePress = r; 
	}
	
	public Boolean getRearPress() {
		return this.rearBrakePress; 
	}
	
	// Set and get the bike's gear
	public void setGearBox(int gb) {
		this.gearbox = gb; 
	}
	
	public int getGearBox() {
		return this.gearbox; 
	}
	
	// Print relevant bike's information
	public void getInformation() {
		System.out.println("-> ID: " + getId() + " Available: " + getAvailable() + " GPS latitude: " + getLatitude() + " GPS longitude: " + getLongitude());
	}
	
}
