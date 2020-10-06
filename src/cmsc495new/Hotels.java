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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

public class Hotels {
    // Database credentials
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
        String numberOfBeds = String.valueOf(Beds.getSelectedItem());
        String bedType = String.valueOf(BedType.getSelectedItem());
                                        
            PreparedStatement stmt = null;
        try {
            try { conn = java.sql.DriverManager.getConnection(DB_URL, USER, PASS); } 
            catch (SQLException ex) { JOptionPane.showMessageDialog(null, "Connection Issue"); }
         
            
        String Guest_Query   = numberOfGuests.contains("Any")  ? "" : " And a.ROOMCAPACITY >= ?";
        String Beds_Query    = numberOfBeds.contains("Any")    ? "" : " And a.BEDNUMBER = ?";
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
                    + Beds_Query
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
            
            if(!Beds_Query.isEmpty())
            {
                stmt.setString(paramIndex, numberOfBeds); 
                paramIndex++;
            }
            
            if(!BedType_Query.isEmpty())
            {
                stmt.setString(paramIndex, bedType); 
                paramIndex++;
            }
            
            ResultSet rs = stmt.executeQuery();
            displayResults(tm, rs);
            conn.close();
        } catch (SQLException ex){ JOptionPane.showMessageDialog(null, "Connection Issue"); }      
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
            JOptionPane.showMessageDialog(null, "Connection Issue");
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
                
                Confirm();
            }
            conn.close();
        } catch (SQLException ex){ Logger.getLogger(Hotels.class.getName()).log(Level.SEVERE, null, ex); }
    }
    
    public static String calculateTotalPrice(String price, String startDate, String endDate){
    //Determines the total price of the reservation    
        NumberFormat formatter = new DecimalFormat("#0.00");  
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate, dtf);
        LocalDate end = LocalDate.parse(startDate, dtf);
        
        long daysBetween = ChronoUnit.DAYS.between(start, end)+1;

        return "$" +(formatter.format(Double.parseDouble((price))*daysBetween));        
    }

    public static void Confirm() {
        try {
            try { conn = java.sql.DriverManager.getConnection(DB_URL, USER, PASS); } 
            catch (SQLException ex) { Logger.getLogger(Hotels.class.getName()).log(Level.SEVERE, null, ex); }
                        
            String sqlQuery = ""
                    + " SELECT"
                    + "  a.CLIENTNAME, a.CHECKINDATE, a.CHECKOUTDATE"
                    + ", b.PRICE"
                    + ", b.ROOMCAPACITY, b.BEDTYPE, b.FEATURES"
                    + " FROM HotelReservations a"
                    + " INNER JOIN Hotels b"
                    + "  on a.HOTELROOMID = b.HOTELROOMID"
                    + " ORDER BY HOTELRESERVATIONID DESC"
                    + " FETCH FIRST ROW ONLY";
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
                stmt.executeQuery();
                
                ResultSet rs = stmt.executeQuery();
                while (rs.next()){
                    String successMessage = ""
                            + "Client: " + rs.getString("CLIENTNAME") +"\n"
                            + "Date: " + rs.getString("CHECKINDATE") + " - " + rs.getString("CHECKOUTDATE") + "\n"
                            + "Total Price: " 
                            + calculateTotalPrice(rs.getString("PRICE"), rs.getString("CHECKINDATE"), rs.getString("CHECKOUTDATE")) + "\n"
                            + "-----------------------------------------------\n"
                            + "Capacity: " + rs.getString("ROOMCAPACITY") + "\n"
                            + "Bed Type: " + rs.getString("BEDTYPE") + "\n"
                            + "Features: " + rs.getString("FEATURES");
                    
                    JOptionPane.showMessageDialog(null, successMessage);
                }               
                
            conn.close();
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Connection Issue");
        }
    }
}


