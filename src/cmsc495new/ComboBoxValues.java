package cmsc495new;

/**
 * @author Adam Santana (Team Bravo)
 * @version 1.0
 * @since 2020-9-19
 * 
 * Description: This class holds all the values for the drop-downs. To add more options to GUI's drop-downs
 *              add then to their respective arrays.
 */

public class ComboBoxValues {
    
    // Values for car panel drop-down
    private String[] passengerCapacity = {"2", "4", "5", "7", "8"};
   
    private String[] bodyType =          {"Convertible", "Sedan", "SUV", "Truck"};
    
    private String[] make =              {"Chevy", "Ford", "Honda", "Mercedes-Benz", 
                                         "Mazda", "Nissan", "Toyota"};
    
    private String[] model =             {"Camaro", "Mustang", "Pilot", "Civic", "GLS", 
                                          "Miata", "Rogue", "Camry", "Tundra", "3"};

    private String[] year =              {"2018", "2020"};
    
    // Values for hotel panel drop-down
    private String[] numberOfGuests =    {"1", "2", "3", "4"};  
   
    private String[] bedType =           {"Full", "Queen", "King", "Double Full", "Double Queen", 
                                          "Double King", "Queen Suite", "King Suite"};
    
    private String[] numberOfBeds =      {"1","2"};
    private String[] Features = {};
    
    // Values for flights panel drop-down
    private String[] airline =           {"Allied Airlines", "NorhtWest Airlines", "Oscar Airlines", 
                                          "Private Chacter"};
    
    private String[] numberOfPasengers = {"1","2","3","4","5","6","7","8","9"};
    
    private String[] flightClass =       {"Economy", "Premium", "1st"};
    
    
    // Getters for arrays
    public String[] getPassengerCapacity() {
        return passengerCapacity;
    }

    public String[] getBodyType() {
        return bodyType;
    }

    public String[] getMake() {
        return make;
    }

    public String[] getYear() {
        return year;
    }

    public String[] getNumberOfGuests() {
        return numberOfGuests;
    }

    public String[] getBedType() {
        return bedType;
    }

    public String[] getNumberOfBeds() {
        return numberOfBeds;
    }

    public String[] getFeatures() {
        return Features;
    }

    public String[] getAirline() {
        return airline;
    }

    public String[] getNumberOfPasengers() {
        return numberOfPasengers;
    }

    public String[] getFlightClass() {
        return flightClass;
    }
    
    public String[] getModel() {
        return model;
    }
}
