package product;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReadShopCSV {

	Map<String, String> products = new HashMap();

	public static void main(String[] args) {
		ReadShopCSV obj = new ReadShopCSV();
		obj.populateShopData();
	}
	
	public Map<String,String> getShopProducts() {
		if(products.isEmpty()) {
			products = populateShopData();
		}
		return products;
	}

	public Map<String,String> populateShopData() {

		String csvFile = "productUpdate.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] product = line.split(cvsSplitBy);
				if(product.length == 3) {
				    int len = product[2].length();
				    String price = product[2].substring(0, len-2);
				    System.out.println("Product [id= " + product[0] + " , price=" + price + "]");
				    products.put(product[0], price);//id , price
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Done: " + products.size());
		System.out.println(products.toString());
		return products;
	}

}