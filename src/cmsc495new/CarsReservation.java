/*
 * Name: Gabrielle Jeuck
 * Class: CMSC 495 - TEAM BRAVO
 * Date: 9/19/2020
 * Purpose: Pull data and make reservations for car data. 
 */
package cmsc495new;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Gabrielle Jeuck
 */
public class CarsReservation {

    // Database credentials
    private static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    private static final String DB_URL = "jdbc:derby://localhost/CMSC495";
    private static final String USER = "bravo";
    private static final String PASS = "bravo";
    private Connection conn = null;
    // Variable declaration
    private String bodyType;
    private String make;
    private String model;
    private String year;
    private String capacity;
    private String pickUp;
    private String checkIn;

    // Generic constructor
    public CarsReservation() {

    }

    // Main constructor to pull data
    public CarsReservation(String capacity, String bodyType,
            String make, String model, String year, String pickUp, String dropOff) {
        this.capacity = capacity;
        this.bodyType = bodyType;
        this.make = make;
        this.model = model;
        this.year = year;
        this.pickUp = pickUp;
        this.checkIn = dropOff;
    }

    // General connection to DB
    private void connectDataBase() {
        final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver";
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException ex) {
            Logger.getLogger(CarsReservation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Displays results based on query to table
    private void displayResults(TableModel tm, ResultSet rs) {
        // clears old results to populate new search hit
        while (tm.getRowCount() > 0) {
            ((DefaultTableModel) tm).removeRow(0);
        }
        int col = tm.getColumnCount();

        try {
            while (rs.next()) {
                Object[] rows = new Object[col];
                for (int i = 1; i <= col; i++) {
                    rows[i - 1] = rs.getObject(i);
                }
                ((DefaultTableModel) tm).insertRow(rs.getRow() - 1, rows);
            }

        } catch (SQLException ex) {
            Logger.getLogger(CarsReservation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Queries available cars based on date selected and vehicle not being reserved
    public void getAllCarsData(TableModel tm) {
        // stores additional where checks, i.e. 'a.passengercapacity', etc.
        Map<String, String> searchQueries = new HashMap<>();
        PreparedStatement stmt = null;
        connectDataBase();
        try {
            String sqlQuery = "SELECT DISTINCT a.*, b.carreservationid, b.checkoutdate, b.checkindate FROM cars a "
                    + "LEFT JOIN carreservations b on (a.carid = b.carid "
                    + "AND (((? BETWEEN b.checkoutdate AND b.checkindate) "
                    + "OR (? BETWEEN b.checkoutdate AND b.checkindate)) "
                    + "OR ((? <= b.checkoutdate "
                    + "AND ? >= b.checkindate)))) "
                    + "WHERE b.carreservationid IS NULL";
            // if combobox is not ANY then add to searchQueries HashMap
            if (!getCapacity().equals("Any")) {
                searchQueries.put("a.passengercapacity", getCapacity());
            }
            if (!getBodyType().equals("Any")) {
                searchQueries.put("a.cartype", getBodyType());
            }
            if (!getMake().equals("Any")) {
                searchQueries.put("a.make", getMake());
            }
            if (!getModel().equals("Any")) {
                searchQueries.put("a.model", getModel());
            }
            if (!getYear().equals("Any")) {
                searchQueries.put("a.caryear", getYear());
            }
            // counter starts at 5 for preparedstatement setStrings
            int i = 5;
            // loops through to see what needs to be added to query 'AND x.xxxx =?' 
            sqlQuery = searchQueries.keySet().stream().map((paramName) -> " AND " + paramName + "=?").reduce(sqlQuery, String::concat);
            // once added prepare statement and setStrings
            stmt = conn.prepareStatement(sqlQuery);
            stmt.setString(1, getPickUp());
            stmt.setString(2, getDropOff());
            stmt.setString(3, getPickUp());
            stmt.setString(4, getDropOff());

            // loop through to see which paramNames were added,
            // if added set index and value to stmt.setString
            for (String paramName : searchQueries.keySet()) {
                if (paramName.equals("a.passengercapacity")) {
                    stmt.setString(i, getCapacity());
                    i++;
                }
                if (paramName.equals("a.cartype")) {
                    stmt.setString(i, getBodyType());
                    i++;
                }
                if (paramName.equals("a.make")) {
                    stmt.setString(i, getMake());
                    i++;
                }
                if (paramName.equals("a.model")) {
                    stmt.setString(i, getModel());
                    i++;
                }
                if (paramName.equals("a.caryear")) {
                    stmt.setString(i, getYear());
                    i++;
                }
            }
            // execute and call to displayResults based on new query
            ResultSet rs = stmt.executeQuery();
            displayResults(tm, rs);
            conn.close();
        } catch (SQLException e) {
            // remove error in final submission used for testing only
            JOptionPane.showMessageDialog(null, "Connection Issue");
        }
    }

    // Confirmation with calculated costs / day then inserts into database
    public String confirmCarReservation(int ID, String name, String pickUpDate, String dropOffDate) {
        NumberFormat formatter = new DecimalFormat("#0.00");     
        String successMessage = "";
        String price ="";
        String carYear = "";
        String carMake = "";
        String carModel = "";
        // used to calculate total cost for receipt
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date1 = LocalDate.parse(pickUpDate, dtf);
        LocalDate date2 = LocalDate.parse(dropOffDate, dtf);
        long daysBetween = ChronoUnit.DAYS.between(date1, date2)+1;
        StringBuilder test = new StringBuilder();
        // remove in final for test purposes
        try {
            connectDataBase();
            String sqlQuery = "select * from cars WHERE carID =?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
                stmt.setInt(1, ID);
                stmt.executeQuery();
                
                ResultSet rs = stmt.executeQuery();
                while (rs.next()){
                    carYear = rs.getString(6);
                    carMake = rs.getString(4);
                    carModel = rs.getString(5);
                    price = rs.getString(7);
                }

                successMessage = "<html><b>Client: </b>" +name
                        + "<br><br><strong>Vehicle:</strong> " + carYear +" "+ carMake+" " + carModel
                        + "<br><br><strong>Pick-Up Date:</strong> "+pickUpDate
                        + "<br><br><strong>Drop-Off Date:</strong> "+dropOffDate 
                        + "<br><br><hr>"
                        + "<strong>\tTotal Price: </strong>$" +(formatter.format(Double.parseDouble((price))*daysBetween))+"</html>";
            conn.close();
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(CarsReservation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return successMessage;
    }
    // inserts into database the final reservation
    public String getConfirmation(String name, int carID, String pickUpDate, String dropOffDate) {
        connectDataBase();
        String sqlQuery = "insert into carreservations (CarID, ClientName, CheckOutDate, CheckInDate) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            stmt.setInt(1, carID);
            stmt.setString(2, name);
            stmt.setString(3, pickUpDate);
            stmt.setString(4, dropOffDate);
            stmt.executeUpdate();
            conn.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(CarsReservation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return confirmCarReservation(carID, name, pickUpDate, dropOffDate);
    }

    // Getters and Setters
    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getPickUp() {
        return pickUp;
    }

    public void setPickUp(String pickUp) {
        this.pickUp = pickUp;
    }

    public String getDropOff() {
        return checkIn;
    }

    public void setDropOff(String checkIn) {
        this.checkIn = checkIn;
    }

}
