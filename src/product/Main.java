package product;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

/*
 * Gets all product skus and prices from shopify
 * Reads all local shop product id and prices
 */
public class Main {

	
	
	Map<String,String> shopify = new HashMap();
	
	private final String USER_AGENT = "Mozilla/5.0";

	public static void main(String[] args) throws Exception {
		Main http = new Main();

		System.out.println("Testing 1 - Send Http GET request");
		http.getAsJson();
	}

	// HTTP POST request
	private void sendPost() throws Exception {

		String url = "";

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);

		// add header
		post.setHeader("User-Agent", USER_AGENT);

		List<String> urlParameters = new ArrayList<String>();
		// urlParameters.add(new BasicNameValuePair("sn", "C02G8416DRJM"));

		// post.setEntity(new UrlEncodedFormEntity(urlParameters));

		HttpResponse response = client.execute(post);
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + post.getEntity());
		System.out.println("Response Code : "
				+ response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		System.out.println(result.toString());
	}

}