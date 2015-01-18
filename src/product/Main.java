package product;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

/*
 * Gets all product skus and prices from shopify
 * Reads all local shop product id and prices
 */
public class Main {

	
	
	static Map<String,String> shopifyUpdates = new HashMap();
	
	private final String USER_AGENT = "Mozilla/5.0";

	public static void main(String[] args) throws Exception {
		ShopifyProducts shopifyProducts = new ShopifyProducts();
		Map<String,String> shopifyProds = shopifyProducts.getProducts();
		
		ReadShopCSV shopCsv = new ReadShopCSV();
		Map<String,String> shopProds = shopCsv.getShopProducts();
		
		//compare prices of each product map
		for(Entry entry: shopifyProds.entrySet()) {
			String productId = (String)entry.getKey();//
			if(shopProds.containsKey(productId)) {
				String price = (String)entry.getValue();
				String updatePrice = shopProds.get(productId);
				if(!price.equals(updatePrice)) {
     				shopifyUpdates.put(productId, updatePrice);
     				System.out.println("Shopify price:" + price + ", shop price: " + updatePrice);
     				System.out.println("Number of product updates:" + shopifyUpdates.size());
				}
			}
		}
	}

	//TODO 
	///Make update call for each product in list
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