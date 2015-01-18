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

	String key = "f5e13afaec53be5d0d17354a7d28d6a0:c4293ab721d144f92ad3a8fdf514ab96";
	String user = "@healing-harvest.myshopify.com/";
	String path = "admin/products.json";

	String shopifyUrl = "https://" + key + user + path;
	Map<String, String> products = new HashMap();

	protected Map<String, String> getProducts() throws Exception {
		if(products.isEmpty()) {
			getProductJson();
		}
		System.out.println("Returning products:");
		System.out.println(products.toString());
		return products;
	}

	private void getProductJson() throws Exception {
		InputStream source = requestProducts();
		Reader reader = new InputStreamReader(source);
		
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(reader);
		element = element.getAsJsonObject().get("products");
		JsonArray productArray = element.getAsJsonArray();
		Iterator it = productArray.iterator();

		while(it.hasNext()) {
			JsonElement product = (JsonElement)it.next(); 
            JsonElement productVariant = product.getAsJsonObject().get("variants");
            JsonElement title = product.getAsJsonObject().get("title");
            String titleStr = title.getAsString();
            System.out.println(titleStr); //TODO log this along with price and sku
            
            JsonArray variantsArray = productVariant.getAsJsonArray();
            Iterator varIt = variantsArray.iterator();
            while(varIt.hasNext()) {
            	JsonElement variant = (JsonElement)varIt.next();
            	JsonElement price = variant.getAsJsonObject().get("price");
            	JsonElement sku = variant.getAsJsonObject().get("sku");
            	String priceStr = price.getAsString();
            	String skuStr = sku.getAsString();
            	System.out.println(priceStr + " , " + skuStr);
            	products.put(skuStr, priceStr);
            }
		}
	}

	private InputStream requestProducts() throws Exception {

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(shopifyUrl);

		HttpResponse response = client.execute(request);

		System.out.println("\nSending 'GET' request to URL : " + shopifyUrl);
		System.out.println("Response Code : "
				+ response.getStatusLine().getStatusCode());

		InputStream inputStream = response.getEntity().getContent();
		return inputStream;
	}


	private void populateProducts() {

	}
}
