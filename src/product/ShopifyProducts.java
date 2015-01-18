package product;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ShopifyProducts {

	Map<String, String> products = new HashMap();

	protected Map<String, String> getProducts() {
		return products;
	}

	private void requestProductJson() {

	}

	private void populateProducts() {

	}

	String key = "f5e13afaec53be5d0d17354a7d28d6a0:c4293ab721d144f92ad3a8fdf514ab96";
	String user = "@healing-harvest.myshopify.com/";
	String path = "admin/products.json";

	String shopifyUrl = "https://" + key + user + path;

	private void getAsJson() throws Exception {
		InputStream source = retrieveStream();
		Reader reader = new InputStreamReader(source);
		
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(reader);
		JsonArray productArray = element.getAsJsonArray();
		Iterator it = productArray.iterator();

		while(it.hasNext()) {
			JsonElement product = (JsonElement)it.next(); 
			//product.getAsJsonArray().
		}
	}
	
	// HTTP GET request
	private InputStream retrieveStream() throws Exception {

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(shopifyUrl);

		HttpResponse response = client.execute(request);

		System.out.println("\nSending 'GET' request to URL : " + shopifyUrl);
		System.out.println("Response Code : "
				+ response.getStatusLine().getStatusCode());

		InputStream inputStream = response.getEntity().getContent();
		return inputStream;
	}
}
