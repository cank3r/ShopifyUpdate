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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Gets all product skus and prices from shopify
 * Reads all local shop product id and prices
 */
public class Main {
	//TODO am passing logback.xml location in vm args
	static Logger logger = LoggerFactory.getLogger(Main.class.getName());
	
	static Map<String,Map<String,String>> shopifyUpdates = new HashMap();
	
	private final String USER_AGENT = "Mozilla/5.0";

	public static void main(String[] args) throws Exception {
		logger.info("Initializing....");
        
		ShopifyProducts shopifyProducts = new ShopifyProducts();
		Map<String,Map<String,String>> shopifyProds = shopifyProducts.getProducts();
		logger.info("Shopify product map created. {} entries", shopifyProds.size());
		
		ReadShopCSV shopCsv = new ReadShopCSV();
		Map<String,String> shopProds = shopCsv.getShopProducts();
		logger.info("Physical Shop product map created. {} entries", shopProds.size());
		
		//compare prices of each product map
		for(Entry entry: shopifyProds.entrySet()) {
			String productId = (String)entry.getKey();//
			if(shopProds.containsKey(productId)) {
				Map data = (Map)entry.getValue();
				String price = (String)data.get("price");
				String quantity = (String)data.get("quantity");
				String updatePrice = shopProds.get(productId);
				if(!price.equals(updatePrice)) {
					Map<String,String> updateData = new HashMap();
					updateData.put("price", updatePrice);
					updateData.put("quantity", quantity);
     				shopifyUpdates.put(productId, updateData);
                    logger.info("Shopify price {} : Physical Shop price {} ", price, updatePrice);
				}
				//TODO also need to do an update based on quantity comparision
			}
		}
        logger.info("Number of product updates required: {}", shopifyUpdates.size());
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