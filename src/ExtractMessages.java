import java.util.Properties;


public class ExtractMessages {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		Properties properties = new Properties();
//		EmailsReader emailReader = new EmailsReader();
//		emailReader.getNewEmails("imap", "imap.gmail.com", "993", "test.karthi23@gmail.com", "Password@123");
	
//		emailReader.getNewEmails("imap", "imap.gmail.com", "993", "sbgmobile4@gmail.com", "Password@");

		String OAuthConsumerKey = "nWugf2uJLzGTJxfEEtnN8sQMm";
		String OAuthConsumerSecret = "9MGX5u4tGFfpIGKdr4RzZOZzsFl1lrwD6y6crrnn5Bgp0ujF8x";
		String OAuthAccessToken = "882913507016155136-at8Hm4rfYJ2ciRtO4geZyLsMYF4E2tM";
		String OAuthAccessTokenSecret = "1oIFyv6XiI0UmPNCe5NNxNZq0evWq8HqrvLsQmtntj1sp";
		TweetsReader tweetsReader = new TweetsReader();
		if(tweetsReader.getTweets(OAuthConsumerKey, OAuthConsumerSecret, OAuthAccessToken, OAuthAccessTokenSecret)==0)
			System.out.println("Tweets are extracted successfully...");
		else
			System.out.println("Tweets are extracted with an exception");
		
		PlayStoreReviewsReader playStoreReviewsReader = new PlayStoreReviewsReader();
		if(playStoreReviewsReader.getReviewsFromPlayStore() ==0)
			System.out.println("All the reviews are extracted successfully...");
		else
			System.out.println("Reviews are extracted with an exception");
		
	
	}


}
