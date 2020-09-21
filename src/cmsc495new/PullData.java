
package cmsc495new;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
/**
 *
 * @author Adam Santana
 * 
 *    ***************************  CLASS FOR TESTING ONLY **************************************
 * 
 */
public class PullData {

    public void getData(TableModel tm, String table) {
        
        String url = "jdbc:derby://localhost:1527/two";
        String userName = "adam"; // Set DB username.
        String pass = "123"; // Set DB Password.
        String query = "SELECT * FROM ADAM." + table;
        Connection con;
        Statement st;
   
        try {
            con = DriverManager.getConnection(url, userName, pass);
            System.out.println("Connection Successful");
            st = con.createStatement();
            ResultSet rs = st.executeQuery(query);

            while(tm.getRowCount()>0)
                ((DefaultTableModel)tm).removeRow(0);
          
            int cols = tm.getColumnCount();
                              
            while(rs.next()){
                Object[] rows = new Object[cols];
                for(int i = 0; i < cols; i++){
                    rows[i] = rs.getObject(i+1);
                }
                
                ((DefaultTableModel)tm).addRow(rows);
            }  
           
            st.close();
            con.close();
        }catch(SQLException e){
            System.out.println("Connection Failed. " + e);             
        }
    }
}
