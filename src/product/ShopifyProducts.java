package product;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ShopifyProducts {

	Logger logger = LoggerFactory.getLogger(ShopifyProducts.class.getName());

	String key = "f5e13afaec53be5d0d17354a7d28d6a0:c4293ab721d144f92ad3a8fdf514ab96";
	String user = "@healing-harvest.myshopify.com/";
	String productPath = "admin/products.json";
	String variantPath = "admin/variants.json";

	String shopifyUrl = "https://" + key + user;
	Map<String, Map<String, String>> products = new HashMap();

	protected Map<String, Map<String,String>> getProducts() throws Exception {
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
            	JsonElement quantity = variant.getAsJsonObject().get("inventory_quantity");
            	JsonElement variantId = variant.getAsJsonObject().get("id");
            	String priceStr = price.getAsString();
            	String skuStr = sku.getAsString();
            	String quantityStr = quantity.getAsString();
            	Integer vid = variantId.getAsInt();
            	Map<String,String> data = new HashMap();
            	data.put("variantId", vid.toString(vid));
            	data.put("price", priceStr);
            	data.put("quantity", quantityStr);
            	products.put(skuStr, data);
            }
		}
	}

	private InputStream requestProducts() throws Exception {

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(shopifyUrl + productPath);

		HttpResponse response = client.execute(request);

		System.out.println("\nSending 'GET' request to URL : " + shopifyUrl);
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

		InputStream inputStream = response.getEntity().getContent();
		return inputStream;
	}

	/*
	PUT /admin/variants/#{id}.json
	{
       "variant": {
       "id": 808950810,
       "price": "99.00"
       }
    }

	PUT /admin/variants/#{id}.json
	{
	  "variant": {
	    "id": 808950810,
	    "inventory_quantity": 100,
	    "old_inventory_quantity": 10
	  }
	}
	*/
	//TODO
	//CHECK POSSIBILITY OF UPDATING PRICE AND QUANTITY IN ONE HTTP REQUEST
	//ONLY PRICE BELOW
	public void updateProductsPrice() throws Exception {

		HttpClient client = new DefaultHttpClient();

		Map<String, Map<String,String>> productUpdateMap = Main.getProductUpdateMap();
		
		for(Entry entry : productUpdateMap.entrySet()) {
			String productId = (String)entry.getKey();
			Map<String,String> data = (Map)entry.getValue();
			String variantId = data.get("variantId");
			String price = data.get("price");
			String quantity = data.get("quantity");
			String variantUrl = shopifyUrl + variantPath + "/" + variantId + ".json";
			logger.info("variantUrl: {}", variantUrl);
			HttpPut request = new HttpPut(variantUrl);
			String json = "{\"variant\": {\"id\": " + variantId +", \"price\":\"" + price + "\" } }";
			logger.info("json for price update: {}", json);
			StringEntity entity = new StringEntity(json); 
			request.setEntity(entity);
			//HttpResponse response = client.execute(request);
    		System.out.println("\nSending 'GET' request to URL : " + shopifyUrl);
		}
	}
}
