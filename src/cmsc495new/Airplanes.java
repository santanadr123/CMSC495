package cmsc495new;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * @author Steven Troxell (Team Bravo)
 * @version 1.0
 * @since 2020-9-23
 *
 * Description: The code for connecting the database with the UI which will
 * allow the user to make an airline reservation.
 */
public class Airplanes {

    /* URL to local database */
    final String url = "jdbc:derby://localhost:1527/CMSC495";
    final String userName = "bravo"; // Set DB username.
    final String pass = "bravo"; // Set DB Password.
    private Connection con; // Database connection object
    private Statement st; // Database statement object for making DB queries

    /* Method to obtain entirety of database to display relevant options 
    in the UI. */
    public void getData(TableModel tm, String aName,
            String fClass, Integer pNum) {

        String query = "SELECT * FROM bravo.airlines"; // Query entire DB

        try {
            con = DriverManager.getConnection(url, userName, pass);
            st = con.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (tm.getRowCount() > 0) {
                ((DefaultTableModel) tm).removeRow(0);
            }

            int cols = tm.getColumnCount();

            while (rs.next()) {
                Object[] rows = new Object[cols];
                for (int i = 0; i < cols; i++) {
                    rows[i] = rs.getObject(i + 1);
                }

                /* Displays only data that is within the user's specified 
                criteria */
                
                
                if ((rows[1].equals(aName) | aName.equals("Any")) 
                        && (rows[2].equals(fClass) | fClass.equals("Any")) 
                        && (Integer.parseInt(rows[6].toString()) >= pNum)) {
                    ((DefaultTableModel) tm).addRow(rows);
                }
            }

            st.close();
            con.close();
        } catch (SQLException e) {
            System.out.println("Connection Failed. " + e);
        }
    }

    // Method to update DB and add reservation to flight reservation DB
    public void updateRecord(Integer pNum, Integer fID, String name) {

        Integer qAvail = 0;
        Integer qRes = 0;
        String depDate = "";
        String arvDate = "";
        String query = "SELECT * FROM bravo.airlines";
        String query2 = 
                "update Airlines set QuantityAvailable=? where FlightID=?";
        String query3 = 
                "update Airlines set QuantityReserved=? where FlightID=?";
        String query4 = "INSERT INTO AirlineReservations VALUES (?,?,?,?,?)";
        PreparedStatement pst2;
        PreparedStatement pst3;
        PreparedStatement pst4;

        try {
            con = DriverManager.getConnection(url, userName, pass);
            st = con.createStatement();
            ResultSet rs = st.executeQuery(query);

            // Search DB for user selected FlighID
            while (rs.next()) {
                int flightID = rs.getInt("FlightID");
                if (flightID == fID) {
                    qAvail = rs.getInt("QuantityAvailable");
                    qRes = rs.getInt("QuantityReserved");
                    depDate = rs.getString("DepartureDate");
                    arvDate = rs.getString("ArrivalDate");
                }
            }

            pst2 = con.prepareStatement(query2);
            pst3 = con.prepareStatement(query3);
            pst4 = con.prepareStatement(query4);

            // Update the quatity of tickets available and reserved in the DB
            pst2.setInt(1, qAvail - pNum);
            pst3.setInt(1, qRes + pNum);
            pst2.setInt(2, fID);
            pst3.setInt(2, fID);
            pst2.executeUpdate();
            pst3.executeUpdate();

            // Add the reservation to the flight reservation database
            pst4.setInt(1, fID);
            pst4.setString(2, name);
            pst4.setString(3, depDate);
            pst4.setString(4, arvDate);
            pst4.setInt(5, pNum);
            pst4.executeUpdate();
            
            // Unsafe SQL statement saved for future reference
            /*st.executeUpdate("INSERT INTO AirlineReservations VALUES ("
                    + fID + ", '" + name + "', '" + depDate + "', '" + arvDate
                    + "', " + pNum + ")");*/

            st.close();
            pst2.close();
            pst3.close();
            pst4.close();
            con.close();
        } catch (SQLException e) {
            System.out.println("Connection Failed. " + e);
        }
    }
}
