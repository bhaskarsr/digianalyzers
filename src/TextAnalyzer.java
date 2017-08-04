import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TextAnalyzer 
{
    public double getSentimentScore(String key, String text) 
    {
        HttpClient httpclient = HttpClients.createDefault();

        try
        {
            URIBuilder builder = new URIBuilder("https://westus.api.cognitive.microsoft.com/text/analytics/v2.0/sentiment");


            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", key);

            String body = 
            	"{ \"documents\": ["
            	+ "{\"language\": \"en\","
                +"\"id\": \""+key+"\","
            	+"\"text\": \""+text +"\"}]}";

            // Request body
            StringEntity reqEntity = new StringEntity(body);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

        	BufferedReader br = new BufferedReader(
            	    new InputStreamReader( 
            	        (entity.getContent())
            	    )
            	);

            	StringBuilder content = new StringBuilder();
            	String line;
            	while (null != (line = br.readLine())) {
            	    content.append(line);
            	}
            	JSONObject obj = new JSONObject(content.toString());
            	JSONArray result = obj.getJSONArray("documents");
            	JSONObject score = result.getJSONObject(0);
            	System.out.println("score: " + score.getDouble("score"));
            	return score.getDouble("score");          
             
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return -1;
        }
		
    }
}
