package product;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ShopifyProducts {

	private static Logger logger = LoggerFactory.getLogger(ShopifyProducts.class.getName());

	private String key;
	private String user;
	private String productPath;
	private String variantPath;

	String shopifyUrl;
	Map<String, Map<String, String>> products = new HashMap();

	HttpClient client = new DefaultHttpClient();

	public ShopifyProducts() {
		loadProperties();
	    shopifyUrl = "https://" + key + user;
	}

	private void loadProperties() {
		Properties prop = new Properties();
    	InputStream input = null;
    	try {
    		String filename = "shop.properties";
    		input = ShopifyProducts.class.getClassLoader().getResourceAsStream(filename);
    		if(input==null){
    	            System.out.println("Sorry, unable to find " + filename);
    		    return;
    		}
            prop.load(input);
            key = prop.getProperty("key");
            user = prop.getProperty("user");
            productPath = prop.getProperty("productPath");
            variantPath = prop.getProperty("variantPath");
    	} catch (IOException ex) {
    		ex.printStackTrace();
        } finally{
        	if(input!=null){
        		try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	}
        }
	}


	protected Map<String, Map<String, String>> getProducts() throws Exception {
		if (products.isEmpty()) {
			getProductJson();
		}
		logger.info("Returning products: {} ", products.size());
		return products;
	}

	private void getProductJson() throws Exception {
		int totalProducts = countShopifyProducts();
		logger.info("Counted {} shopify products", totalProducts);
		double requests = totalProducts / 250;// shopify only returns 250 in one call
		for (int i = 0; i <= requests; i++) {
			updateProductMap(i+1);
		}
	}

	private void updateProductMap(int page) throws Exception {
		InputStream source = requestProducts(page);
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

	public Integer countShopifyProducts() throws ClientProtocolException,
			IOException {
		HttpGet countRequest = new HttpGet(shopifyUrl
				+ "/admin/products/count.json");
		HttpResponse response = client.execute(countRequest);
		String prods = EntityUtils.toString(response.getEntity());

		Gson gson = new Gson();
		ProductCount products = gson.fromJson(prods, ProductCount.class);

		if (products != null) {
			return Integer.parseInt(products.getCount());
		}
		return -1;
	}

	private InputStream requestProducts(int page) throws Exception {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(shopifyUrl + productPath + page );

		logger.info("Requesting shopify products {}", request.getURI());
		HttpResponse response = client.execute(request);
		Header header = response.getFirstHeader("HTTP_X_SHOPIFY_SHOP_API_CALL_LIMIT");
		if(header != null) {//requests throttled, so slow down if over 40 limit
			String apiCalls = header.getValue();
			String made = apiCalls.split("/")[0];
			if(Integer.parseInt(made) > 30) {
				Thread.sleep(100000);
			}
		}
		InputStream inputStream = response.getEntity().getContent();
		return inputStream;
	}

	/*
	 * PUT /admin/variants/#{id}.json { "variant": { "id": 808950810, "price":
	 * "99.00" } }
	 * 
	 * PUT /admin/variants/#{id}.json { "variant": { "id": 808950810,
	 * "inventory_quantity": 100, "old_inventory_quantity": 10 } }
	 */
	// TODO
	// CHECK POSSIBILITY OF UPDATING PRICE AND QUANTITY IN ONE HTTP REQUEST
	// ONLY PRICE BELOW
	public void updateProductsPrice() throws Exception {

		Map<String, Map<String, String>> productUpdateMap = Main
				.getProductUpdateMap();

		for (Entry entry : productUpdateMap.entrySet()) {
			String productId = (String) entry.getKey();
			Map<String, String> data = (Map) entry.getValue();
			String variantId = data.get("variantId");
			String price = data.get("price");
			String quantity = data.get("quantity");
			String variantUrl = shopifyUrl + variantPath + "/" + variantId
					+ ".json";

			logger.info("variant url: {}", variantUrl);
			HttpPut request = new HttpPut(variantUrl);
			request.setHeader("Content-Type", "application/json");

			updatePrice(request, variantId, price);
			updateQuantity(request, variantId, quantity);
		}
	}

	private void updatePrice(HttpPut request, String variantId, String price)
			throws Exception {
		String json = "{\"variant\": {\"id\": \"" + variantId
				+ "\", \"price\":\"" + price + "\" } }";
		StringEntity entity = new StringEntity(json);
		request.setEntity(entity);
		HttpResponse response = client.execute(request);
		logger.info("HTTP Status: {}. json for price update: {}",
				response.getStatusLine(), json);
		if (response.getEntity() != null) {
			response.getEntity().consumeContent();
		}
	}

	private void updateQuantity(HttpPut request, String variantId,
			String quantity) throws Exception {
		String json = "{\"variant\": {\"id\": \"" + variantId
				+ "\", \"inventory_quantity\":\"" + quantity
				+ "\",\"old_inventory_quantity\":\"" + quantity + "\" } }";
		StringEntity entity = new StringEntity(json);
		request.setEntity(entity);
		HttpResponse response = client.execute(request);
		logger.info("HTTP Status: {}.  json for quantity update: {}",
				response.getStatusLine(), json);
		if (response.getEntity() != null) {
			response.getEntity().consumeContent();
		}
	}
}
