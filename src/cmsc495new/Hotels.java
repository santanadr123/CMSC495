/**********************************************************************************************************************
 Filename: Hotels.java
 Author:   Jay Ayers
 Date:     9/27/2020

 Purpose: Hotels.java will do the following -
 * 1. checkDateRange() - Check the date range to see if -
 *    a. Date range has been selected
 *    b. Date range is in proper format (e.g. Check-In date is before Check-Out date).
 * 2. Search() - Searches hotels that are available for reservation. Places results in selection table.
 * 3. Add() - Adds reservation from the selection table.
 *********************************************************************************************************************/

package cmsc495new;
        
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.text.DateFormat;  
import java.text.SimpleDateFormat;
import java.util.Date;

import com.toedter.calendar.JDateChooser;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

public class Hotels {
    // Database credentials
    private static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    private static final String DB_URL = "jdbc:derby://localhost/CMSC495";
    private static final String USER = "bravo";
    private static final String PASS = "bravo";
    private static Connection conn = null;
        
    public static String getDateFormat(JDateChooser thisDate) {
    //Converts date format from JDateChooser to a string of "MM/dd/yyyy"
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");  
        String strDate = dateFormat.format(thisDate.getDate());  
        
        return strDate;
    }    
    
    public static String checkDateRange(Date checkInDate, Date checkOutDate){
    //Check the date range to see if -
    //    a. Date range has been selected
    //    b. Date range is in proper format (e.g. Check-In date is before Check-Out date)
         
        String Message = "";
        
        //Check For Nulls
        if(checkInDate == null)
            Message += "The 'Check-In' date is required.\n";
        
        if(checkOutDate == null)
            Message += "The 'Check-Out' date is required.\n";
                
        if(!Message.isEmpty())
            return Message;
        
        //Check if Check-In date is after Check-Out date
        else if(checkInDate.after(checkOutDate))
            Message = "The 'Check-In' date must be before 'Check-Out' date.";
 
       return Message;
    }
        
    public static void Search(JComboBox<String> Guests, JComboBox<String> Beds, JComboBox<String> BedType
            , JDateChooser checkInDate, JDateChooser checkOutDate
            , TableModel tm){
    //Searches hotels that are available for reservation. Places results in selection table.
        
        String numberOfGuests = String.valueOf(Guests.getSelectedItem());
        //String numberOfBeds = String.valueOf(Beds.getSelectedItem());
        String bedType = String.valueOf(BedType.getSelectedItem());
                                        
            PreparedStatement stmt = null;
        try {
            try { conn = java.sql.DriverManager.getConnection(DB_URL, USER, PASS); } 
            catch (SQLException ex) { Logger.getLogger(CarsReservation.class.getName()).log(Level.SEVERE, null, ex); }
         
            
        String Guest_Query   = numberOfGuests.contains("Any")  ? "" : " And a.ROOMCAPACITY = ?";
        //String Beds_Query    = numberOfBeds.contains("Any")    ? "" : " And a.ROOMCAPACITY >= ?";
        String BedType_Query = bedType.contains("Any") ? "" : " And a.BedType = ?";
        
            String sqlQuery = ""
                    + "SELECT DISTINCT a.* "
                    + "FROM hotels a "
                    + " LEFT JOIN hotelReservations b "
                    + "  on "
                    + "( " 
                    + " a.HOTELROOMID = b.HOTELROOMID "
                    + "  AND "
                    + " ( "
                    + "  ( " //Between Clause
                    + "    (? BETWEEN b.CHECKINDATE AND b.CHECKOUTDATE) "
                    + "     OR "
                    + "     (? BETWEEN b.CHECKINDATE AND b.CHECKOUTDATE) "
                    + "  )"
                    + "  OR " //Straddle Date Clause
                    + "  ( "
                    + "     (? <= b.CHECKINDATE AND ? >= b.CHECKOUTDATE) "
                    + "  ) "
                    + " ) "
                    +" )" 
                    + "WHERE b.HOTELRESERVATIONID IS NULL"
                    + Guest_Query
                    //+ Beds_Query
                    + BedType_Query;
            
            stmt = conn.prepareStatement(sqlQuery);
            
            stmt.setString(1, getDateFormat(checkInDate));
            stmt.setString(2, getDateFormat(checkOutDate));
            stmt.setString(3, getDateFormat(checkInDate));
            stmt.setString(4, getDateFormat(checkOutDate));
            
            int paramIndex = 5;
            if(!Guest_Query.isEmpty())
            {
                stmt.setString(paramIndex, numberOfGuests); 
                paramIndex++;
            }
            
            /*
            if(!Beds_Query.isEmpty())
            {
                stmt.setString(paramIndex, numberOfBeds); 
                paramIndex++;
            }
            */
            
            if(!BedType_Query.isEmpty())
            {
                stmt.setString(paramIndex, bedType); 
                paramIndex++;
            }
            
            ResultSet rs = stmt.executeQuery();
            displayResults(tm, rs);
            conn.close();
        } catch (SQLException ex){ Logger.getLogger(Hotels.class.getName()).log(Level.SEVERE, null, ex); }      
    }
    
    private static void displayResults(TableModel tm, ResultSet rs) {
    //Displays results in a selection table
        // clears old results to populate new search hit
        while (tm.getRowCount() > 0) { ((DefaultTableModel) tm).removeRow(0); }
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
            Logger.getLogger(Hotels.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void Add(int ID, String ClientName, JDateChooser checkInDate, JDateChooser checkOutDate) {
    //Adds reservation from the selection table. 
        try {
            try { conn = java.sql.DriverManager.getConnection(DB_URL, USER, PASS); } 
            catch (SQLException ex) { Logger.getLogger(CarsReservation.class.getName()).log(Level.SEVERE, null, ex); }
            
            String sqlQuery = "INSERT INTO hotelReservations (HotelRoomID, ClientName, CheckInDate, CheckOutDate) "
                            + "VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
                stmt.setInt(1, ID);
                stmt.setString(2, ClientName);
                stmt.setString(3, getDateFormat(checkOutDate));
                stmt.setString(4, getDateFormat(checkInDate));
                stmt.executeUpdate();
            }
            conn.close();
        } catch (SQLException ex){ Logger.getLogger(Hotels.class.getName()).log(Level.SEVERE, null, ex); }
    }
}


