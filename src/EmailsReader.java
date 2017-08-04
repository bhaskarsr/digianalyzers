import java.util.Date;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags.Flag;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import javax.print.attribute.standard.DateTimeAtCompleted;

import com.mysql.jdbc.PreparedStatement;

import javax.mail.Multipart;
import javax.activation.DataHandler;
import java.io.IOException;
import javax.mail.Flags;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class EmailsReader {

	public static Properties getServerProperties(String protocol, String host, String port) {
		Properties properties = new Properties();
		properties.put(String.format("mail.%s.host", protocol), host);
		properties.put(String.format("mail.%s.port", protocol), port);
		properties.setProperty(String.format("mail.%s.socketFactory.class", protocol),
				"javax.net.ssl.SSLSocketFactory");
		properties.setProperty(String.format("mail.%s.socketFactory.fallback", protocol), "false");
		properties.setProperty(String.format("mail.%s.socketFactory.port", protocol), String.valueOf(port));

		return properties;
	}

	public static void getNewEmails(String protocol, String host, String port, String userName, String password) {
		try {

			Properties properties = getServerProperties(protocol, host, port);
			Session emailSession = Session.getDefaultInstance(properties);

			Store store = emailSession.getStore(protocol);
			store.connect(host, userName, password);

			Folder emailFolder = store.getFolder("Inbox");
			emailFolder.open(Folder.READ_WRITE);
			System.out.println("Before searching");
			Flags seen = new Flags(Flags.Flag.SEEN);
			FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
			Message messages[] = emailFolder.search(unseenFlagTerm);
			System.out.println("Number of new emails " + messages.length);
			if(messages.length >0){
				Connection conn = null;
				Statement stmt = null;
				System.out.println("reading");
				for (int m = 0; m < messages.length; m++) {
					Message newMessage = messages[m];
					if( !newMessage.isMimeType("multipart/*")) {
///						System.out.println("Culprit Email");
					
					Multipart mimeMultipart = (Multipart) newMessage.getContent();
							//					MimeMultipart mimeMultipart = (MimeMultipart) newMessage.getContent();
					BodyPart bodyPart = mimeMultipart.getBodyPart(0);
					String emailContent;
					Date receivedOn = new Date(); 
					String receivedFrom;
					double sentiment;
					///Start here
					//Check the DB and integrate!
					TextAnalyzer textAnalyzer = new TextAnalyzer();
					if (bodyPart.isMimeType("text/plain")) {
						emailContent = (String) bodyPart.getContent();
						receivedOn = newMessage.getReceivedDate();
						receivedFrom = newMessage.getFrom()[0].toString();
						receivedFrom = receivedFrom.substring(receivedFrom.indexOf('<')+1, receivedFrom.indexOf('>'));
						sentiment = textAnalyzer.getSentimentScore("60452481ef184e1e965e5a0cba14456a", emailContent);
						System.out.println("Received on "+receivedOn);
						System.out.println("Received From "+ receivedFrom);
						System.out.println("Email content : "+emailContent);
						System.out.println("Sentiment: "+ sentiment);
						
						Class.forName("com.mysql.jdbc.Driver");
						conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/woema?verifyServerCertificate=false&useSSL=true", "root", "password123");
						stmt = conn.createStatement();
						java.sql.PreparedStatement prepStmt;
						String insertQuery = "insert into messages(category, channel, received_from, received_on, message, sentiment)"		
								          + "values(?, ?, ?, ?, ?, ?)";
						String sql = "SELECT * FROM CATEGORIES WHERE CATEGORY_DESC <> 'MISC'";
						ResultSet rs = stmt.executeQuery(sql);
						
				rsloop: while(rs.next()){
							if(emailContent.toUpperCase().contains(rs.getString("category_desc").toUpperCase())){
								System.out.println("Category Id: " + rs.getString("category_id"));
								System.out.println("Category Name: " + rs.getString("category_desc"));
								prepStmt = conn.prepareStatement(insertQuery);
								prepStmt.setInt(1, rs.getInt("category_id"));
								prepStmt.setString(2, "EMAIL");
								prepStmt.setString(3, receivedFrom);
								java.text.SimpleDateFormat sdf = 
									     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								String currentTime = sdf.format(receivedOn);
								prepStmt.setString(4, currentTime );
								prepStmt.setNString(5, emailContent);
								prepStmt.setDouble(6, sentiment);
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
							prepStmt.setString(2, "EMAIL");
							prepStmt.setString(3, receivedFrom);
							java.text.SimpleDateFormat sdf = 
								     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String currentTime = sdf.format(receivedOn);
							prepStmt.setString(4, currentTime );
							prepStmt.setNString(5, emailContent);
							prepStmt.setDouble(6, sentiment);
							prepStmt.execute();
																					
						}
			
						newMessage.setFlag(Flag.SEEN, true);
						conn.close();
															
					} else {
						System.out.println("No mail contents available...");
					}
					}//if ends
				}
					
				System.out.println("All the new emails are extracted...");
			}
			else{
				System.out.println("No new emails received...");
			}
			emailFolder.close(false);
			store.close();
		
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
