import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class UpdateSentiments {
	
	public void updateSentiments() throws Exception{
		
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/woema?verifyServerCertificate=false&useSSL=true", "root", "bhaskars");
		Statement stmt = conn.createStatement();
		String sql = "SELECT * FROM MESSAGES WHERE SENTIMENT IS NULL OR SENTIMENT = 99";
		ResultSet rs = stmt.executeQuery(sql);
	
	
	}
	

}
