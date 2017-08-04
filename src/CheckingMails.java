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
import javax.mail.Multipart;
import javax.activation.DataHandler;
import java.io.IOException;
import javax.mail.Flags;


public class CheckingMails {

	private static SearchTerm unseenFlagTerm;

	public static void check(String host, String storeType, String user,
			String password) 
	{
		try {

			Properties properties = new Properties();
			properties.put("mail.pop3.host", host);
			properties.put("mail.pop3.port", "995");
			properties.put("mail.pop3.starttls.enable", "true");
			Session emailSession = Session.getDefaultInstance(properties);
			Store store = emailSession.getStore("pop3s");
			store.connect(host, user, password);
			Folder emailFolder = store.getFolder("inbox");
			emailFolder.open(Folder.READ_WRITE);
			
			System.out.println("Unread message count: " + emailFolder.getUnreadMessageCount());
			Message messages[] = emailFolder.search(new FlagTerm(new Flags(Flag.SEEN), false));
			if (emailFolder.getUnreadMessageCount() == 0) 
			{
			 System.out.println("No new messages found."); 
			 }
			else 
			{
	    for (int i = 0, n = messages.length; i < n; i++) {
			Message message = messages[i];
			System.out.println("-------------------------------");
			System.out.println("Email Number " + (i + 1));
			System.out.println("Subject: " + message.getSubject());
			System.out.println("From: " + message.getFrom()[0]);
			System.out.println("Date: " + message.getSentDate());
			System.out.println("Text: " + message.getContent().toString());
			
			Flags seen = new Flags(Flags.Flag.SEEN);
		     FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
		      Message messages1[] = emailFolder.search(unseenFlagTerm);
		      for (int i1 = 0; i1 < messages1.length; i++) 
		      	{	    	  
		    	 MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
		    	 BodyPart bodyPart = mimeMultipart.getBodyPart(i1);
		    	 String result= "";
			      if (bodyPart.isMimeType("text/plain")) 
			      { 	
			    	  result = result + "\n" + bodyPart.getContent();
			    	  System.out.println("Content" +result);
			    	  message.setFlag(Flag.SEEN, true);
			    	  break;
			       }
		      else 
		      {
		    	  System.out.println("No mail contents available");
		        }
	         }	      
	     }
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

	public static void main(String[] args) {

		String host = "pop.gmail.com";
		String mailStoreType = "pop3";
//		String username = "test.karthi23@gmail.com";
//		String password = "Password@123";
		String username = "sbgmobile4@gmail.com";
		String password = "Password@";
		String message = "";
		check(host, mailStoreType, username, password);
	}

}