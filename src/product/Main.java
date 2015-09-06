package product;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
				String vid = (String)data.get("variantId");
				String price = (String)data.get("price");
				String quantity = (String)data.get("quantity");
				JsonParser parser = new JsonParser();
				JsonObject update = (JsonObject)parser.parse(shopProds.get(productId));
				String updatePrice = update.get("price").getAsString();
				String updateQuantity = update.get("quantity").getAsString();
				boolean isPriceUpdate = !price.equals(updatePrice);
				boolean isQuantityUpdate = !quantity.equals(updateQuantity);
				if(isPriceUpdate || isQuantityUpdate) {
					Map<String,String> updateData = new HashMap();
					updateData.put("variantId", vid);
					updateData.put("price", updatePrice);
					updateData.put("quantity", updateQuantity);
					updateData.put("isPriceUpdate", String.valueOf(isPriceUpdate));
					updateData.put("isQuantityUpdate", String.valueOf(isQuantityUpdate));
     				shopifyUpdates.put(productId, updateData);
                    logger.info("Shopify product id {} - data {}", productId, updateData.toString());
                    logger.info("Shopify price {} : Physical Shop price {} ", price, updatePrice);
                    logger.info("Shopify quantity {} : Physical Shop quantity {} ", quantity, updateQuantity);
				}
				//TODO also need to do an update based on quantity comparision
			}
		}
        logger.info("Number of product updates required: {}", shopifyUpdates.size());
        
        shopifyProducts.updateProductsPrice();
	}

	public static Map<String, Map<String,String>> getProductUpdateMap() {
		return shopifyUpdates;
	}
}