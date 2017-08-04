import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;

public class TweetsReader {

//	public static void main(String[] args) throws TwitterException {
//		
//		ConfigurationBuilder cf = new ConfigurationBuilder();
//		
//		cf.setDebugEnabled(true)
//				.setOAuthConsumerKey("nWugf2uJLzGTJxfEEtnN8sQMm")
//				.setOAuthConsumerSecret("9MGX5u4tGFfpIGKdr4RzZOZzsFl1lrwD6y6crrnn5Bgp0ujF8x")
//				.setOAuthAccessToken("882913507016155136-at8Hm4rfYJ2ciRtO4geZyLsMYF4E2tM")
//				.setOAuthAccessTokenSecret("1oIFyv6XiI0UmPNCe5NNxNZq0evWq8HqrvLsQmtntj1sp");
//		
//		TwitterFactory tf = new TwitterFactory(cf.build());
//		twitter4j.Twitter twitter = tf.getInstance();
//		Date lastMentionedDate = new Date();
//		
////		List<Status> status = twitter.getHomeTimeline();
//		List<Status> mentions = twitter.getMentionsTimeline();
////		List<Status> status twitter.getDirectMessages()
//		System.out.println("Output: ");
//		for(Status st : mentions)
//		{
//				
//			lastMentionedDate = st.getCreatedAt();
//			if(st.getCreatedAt().after(lastMentionedDate))
//			{
//				System.out.println(lastMentionedDate);
//				System.out.println(st.getUser().getName()+"------------"+st.getText());
//			}
//			else {
//				
//				System.out.println("No new mentions...");
//			}
//		}
				
//	}

	
	@SuppressWarnings("resource")
	public int getTweets(String OAuthConsumerKey, String OAuthConsumerSecret, String OAuthAccessToken, String OAuthAccessTokenSecret)  {

		try {

			ConfigurationBuilder cf = new ConfigurationBuilder();

			cf.setDebugEnabled(true)
			.setOAuthConsumerKey(OAuthConsumerKey)
			.setOAuthConsumerSecret(OAuthConsumerSecret)
			.setOAuthAccessToken(OAuthAccessToken)
			.setOAuthAccessTokenSecret(OAuthAccessTokenSecret);

			TwitterFactory tf = new TwitterFactory(cf.build());
			twitter4j.Twitter twitter = tf.getInstance();
			Date lastMentionedDate = new Date();
            /*Connect to MySQL */
			Connection conn = null;
			Statement stmt = null;
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/woema?verifyServerCertificate=false&useSSL=true", "root", "password123");
			stmt = conn.createStatement();
			java.sql.PreparedStatement prepStmt;
			String insertQuery = "insert into messages(category, channel, received_from, received_on, message, sentiment)"		
					          + "values(?, ?, ?, ?, ?, ?)";
			String sql = "select COALESCE(max(received_on), '01-01-00') last_received_date from woema.messages where channel = \"TWITTER\"";
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()) {
				System.out.println("Last received date: " + rs.getString("last_received_date"));
				lastMentionedDate = rs.getTimestamp("last_received_date");
				
			}
			
			sql = "SELECT * FROM CATEGORIES WHERE CATEGORY_DESC <> 'MISC'";
			rs = stmt.executeQuery(sql);
			
			List<Status> mentions = twitter.getMentionsTimeline();
			TextAnalyzer textAnalyzer =  new TextAnalyzer();

//			System.out.println("Output: ");
			for(Status st : mentions)
			{
				rs = stmt.executeQuery(sql);
//				System.out.println(lastMentionedDate);
				if(st.getCreatedAt().after(lastMentionedDate))
				{
					String receivedText = st.getText();
					String receivedFrom = st.getUser().getScreenName();
					Long userId = st.getUser().getId();
					System.out.println("User Id :" +userId);
					Date receivedOn = st.getCreatedAt();
					Double sentiment = textAnalyzer.getSentimentScore("60452481ef184e1e965e5a0cba14456a", receivedText);
//					System.out.println(lastMentionedDate);
//					System.out.println(st.getUser().getName()+"------------"+receivedText);
					prepStmt = conn.prepareStatement(insertQuery);
					rsloop: while(rs.next()){
						if(receivedText.toUpperCase().contains(rs.getString("category_desc").toUpperCase())){
							System.out.println("Category Id: " + rs.getString("category_id"));
							System.out.println("Category Name: " + rs.getString("category_desc"));
							prepStmt = conn.prepareStatement(insertQuery);
							prepStmt.setInt(1, rs.getInt("category_id"));
							prepStmt.setString(2, "TWITTER");
							prepStmt.setString(3, receivedFrom);
//							prepStmt.setDate(4, st.getCreatedAt());
							java.text.SimpleDateFormat sdf = 
								     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String currentTime = sdf.format(receivedOn);
							prepStmt.setString(4, currentTime );
							prepStmt.setNString(5, receivedText);
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
						prepStmt.setString(2, "TWITTER");
						prepStmt.setString(3, receivedFrom);
//						prepStmt.setDate(4, st.getCreatedAt());
						java.text.SimpleDateFormat sdf = 
							     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String currentTime = sdf.format(receivedOn);
						prepStmt.setString(4, currentTime );
						prepStmt.setNString(5, receivedText);
						prepStmt.setDouble(6, sentiment);
						prepStmt.execute();
																				
					}
					
						
				}
				
			
			}
			rs.close();
			return 0;
		} catch(Exception e) {
			e.printStackTrace();
			return 1;
		}


	}


//	public static void getNewTweets(String OAuthConsumerKey, String OAuthConsumerSecret, String OAuthAccessToken, String OAuthAccessTokenSecret, String protocol, String host, String userName, String password) {
//		try {
//
//			List<Status> tweets = getTweets(OAuthConsumerKey, OAuthConsumerSecret, OAuthAccessToken, OAuthAccessTokenSecret);
//			Session emailSession = Session.
//			Session emailSession = Session.getDefaultInstance(tweets);
//
//			Store store = emailSession.getStore(protocol);
//			store.connect(host, userName, password);
//
//			Folder emailFolder = store.getFolder("inbox");
//			emailFolder.open(Folder.READ_WRITE);
//			System.out.println("Before searching");
//			Flags seen = new Flags(Flags.Flag.SEEN);
//			FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
//			Message messages[] = emailFolder.search(unseenFlagTerm);
//			System.out.println("Number of new emails " + messages.length);
//			if(messages.length >0){
//				Connection conn = null;
//				Statement stmt = null;
//				System.out.println("reading");
//				for (int m = 0; m < messages.length; m++) {
//					Message newMessage = messages[m];
//					MimeMultipart mimeMultipart = (MimeMultipart) newMessage.getContent();
//					BodyPart bodyPart = mimeMultipart.getBodyPart(0);
//					String emailContent;
//					Date receivedOn = new Date(); 
//					String receivedFrom;
//					double sentiment;
//					///Start here
//					//Check the DB and integrate!
//					TextAnalyzer textAnalyzer = new TextAnalyzer();
//					if (bodyPart.isMimeType("text/plain")) {
//						emailContent = (String) bodyPart.getContent();
//						receivedOn = newMessage.getReceivedDate();
//						receivedFrom = newMessage.getFrom()[0].toString();
//						receivedFrom = receivedFrom.substring(receivedFrom.indexOf('<')+1, receivedFrom.indexOf('>'));
//						sentiment = textAnalyzer.getSentimentScore("60452481ef184e1e965e5a0cba14456a", emailContent);
//						System.out.println("Received on "+receivedOn);
//						System.out.println("Received From "+ receivedFrom);
//						System.out.println("Email content : "+emailContent);
//						System.out.println("Sentiment: "+ sentiment);
//						
//						Class.forName("com.mysql.jdbc.Driver");
//						conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/woema?verifyServerCertificate=false&useSSL=true", "root", "password123");
//						stmt = conn.createStatement();
//						java.sql.PreparedStatement prepStmt;
//						String insertQuery = "insert into messages(category, channel, received_from, received_on, message, sentiment)"		
//								          + "values(?, ?, ?, ?, ?, ?)";
//						String sql = "SELECT * FROM CATEGORIES WHERE CATEGORY_DESC <> 'MISC'";
//						ResultSet rs = stmt.executeQuery(sql);
//						
//				rsloop: while(rs.next()){
//							if(emailContent.toUpperCase().contains(rs.getString("category_desc").toUpperCase())){
//								System.out.println("Category Id: " + rs.getString("category_id"));
//								System.out.println("Category Name: " + rs.getString("category_desc"));
//								prepStmt = conn.prepareStatement(insertQuery);
//								prepStmt.setInt(1, rs.getInt("category_id"));
//								prepStmt.setString(2, "EMAIL");
//								prepStmt.setString(3, receivedFrom);
//								java.text.SimpleDateFormat sdf = 
//									     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//								String currentTime = sdf.format(receivedOn);
//								prepStmt.setString(4, currentTime );
//								prepStmt.setNString(5, emailContent);
//								prepStmt.setDouble(6, sentiment);
//								prepStmt.execute();
//								break rsloop;
//								
//							}
//						}
//						if (rs.isAfterLast()) {
//							System.out.println("No suitable category found. Need to add the msg under misc");
//							sql = "SELECT * FROM CATEGORIES WHERE CATEGORY_DESC = 'MISC'";
//							rs = stmt.executeQuery(sql);
//							rs.next();
//							prepStmt = conn.prepareStatement(insertQuery);
//							prepStmt.setInt(1, rs.getInt("category_id"));
//							prepStmt.setString(2, "EMAIL");
//							prepStmt.setString(3, receivedFrom);
//							java.text.SimpleDateFormat sdf = 
//								     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//							String currentTime = sdf.format(receivedOn);
//							prepStmt.setString(4, currentTime );
//							prepStmt.setNString(5, emailContent);
//							prepStmt.setDouble(6, sentiment);
//							prepStmt.execute();
//																					
//						}
//			
//						newMessage.setFlag(Flag.SEEN, true);
//											
//						
//					} else {
//						System.out.println("No mail contents available...");
//					}
//				}
//				conn.close();	
//				System.out.println("All the new emails are extracted...");
//			}
//			else{
//				System.out.println("No new emails received...");
//			}
//			emailFolder.close(false);
//			store.close();
//		
//		} catch (NoSuchProviderException e) {
//			e.printStackTrace();
//		} catch (MessagingException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
}






