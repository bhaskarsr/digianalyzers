import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import com.mysql.jdbc.PreparedStatement;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;

import javax.activation.DataHandler;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.lang.*;
public class PlayStoreReviewsReader {
	
	public int getReviewsFromPlayStore() {
    	try {
    	    int Rating;
    	    String Review_text;
 //        String csvFile = "/Users/karthikeyanikuppuraj/Downloads/ExcelFormat_dr.csv";
    	    String csvFile = "/Users/aniruddha/eclipse-workspace/Woema/ExcelFormat_dr.csv";
    	    
//    	    String csvFile = "/Users/aniruddha/eclipse-workspace/Woema/Review_May2017 2.csv";
    	    TextAnalyzer textAnalyzer = new TextAnalyzer();
        Double sentiment;
        
        CSVReader reader = null;
        
            reader = new CSVReader(new FileReader(csvFile));
            String[] line;
            reader.readNext();
            while ((line = reader.readNext()) != null)
            
          {
            System.out.println("Received_on=" + line[0] + "   Rating = " + line[1] + " Review_text=" + line[2] );
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            String dateInString = line[0].replaceAll("/", "-");
            System.out.println(dateInString);
            System.out.println(formatter.parse("31-05-2017 17:32"));
            java.util.Date Received_on = formatter.parse(dateInString);
//            java.util.Date Received_on = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(line[0]);
//            Received_on=Integer.parseInt(line[0]);
            Rating=Integer.parseInt(line[1]);
            Review_text = line[2];
            
            Connection conn = null;
            Statement stmt = null;
           
           sentiment = textAnalyzer.getSentimentScore("60452481ef184e1e965e5a0cba14456a",Review_text);
        
           Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/woema?verifyServerCertificate=false&useSSL=true", "root", "password123");
			stmt = conn.createStatement();
			java.sql.PreparedStatement prepStmt;
			String insertQuery = "insert into messages(category, channel, received_from, received_on, message, sentiment, rating)"		
					          + "values(?, ?, ?, ?, ?, ?, ?)";
			String sql = "SELECT * FROM CATEGORIES WHERE CATEGORY_DESC <> 'MISC'";
			ResultSet rs = stmt.executeQuery(sql);
            	
			rsloop: while(rs.next()){
				if(Review_text.toUpperCase().contains(rs.getString("category_desc").toUpperCase())){
					System.out.println("Category Id: " + rs.getString("category_id"));
					System.out.println("Category Name: " + rs.getString("category_desc"));
					prepStmt = conn.prepareStatement(insertQuery);
					prepStmt.setInt(1, rs.getInt("category_id"));
					prepStmt.setString(2, "PLAYSTORE");
					prepStmt.setString(3, "");
					java.text.SimpleDateFormat sdf = 
						     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String currentTime = sdf.format(Received_on);
					prepStmt.setString(4, currentTime );
					if(Review_text.isEmpty()) {
						prepStmt.setNString(5, "Text Not available");
					}	
					else if(Review_text == null) { 
						prepStmt.setNString(5, "Text Not available");
					}	
					else {
						prepStmt.setNString(5, Review_text);
					}	
					prepStmt.setDouble(6, sentiment);
					prepStmt.setInt(7, Rating);
					prepStmt.execute();
					break rsloop;
					
				}
			}
			
            
            
			if (rs.isAfterLast()) {
				System.out.println("No suitable category found. Need to add the msg under misc");
				sql = "SELECT * FROM CATEGORIES WHERE CATEGORY_DESC = 'MISC'";
				rs = stmt.executeQuery(sql);
				rs.next();
				prepStmt = conn.prepareStatement(insertQuery);
				prepStmt.setInt(1, rs.getInt("category_id"));
				prepStmt.setString(2, "PLAYSTORE");
				prepStmt.setString(3, "");
				java.text.SimpleDateFormat sdf = 
					     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String currentTime = sdf.format(Received_on);
				prepStmt.setString(4, currentTime );
				if(Review_text.isEmpty())
					prepStmt.setNString(5, "Review text Not available");
				else if(Review_text == null) 
					prepStmt.setNString(5, "Review text Not available");
				else
					prepStmt.setNString(5, Review_text);
				prepStmt.setDouble(6, sentiment);
				prepStmt.setInt(7, Rating);
				prepStmt.execute();
																		
					}
			  
				}
            
          return 0;
        }
        catch(Exception e) {
        	e.printStackTrace();
        	return 1;
        }
    }

}
