# ShopifyUpdate
We run a small physical store, we also sell online with Shopify.  This project is what we use to keep our shopify store up
to date.  

The flow is simple:
 * create a scheduled task on our pysical store.
 * the task runs an sql query and exports to .csv file.
 * this project code is run taking the .csv as input.
 * it compares the .csv to the shopify inventory and updates shopify as necessary.
  
It may prove useful as a starting point for others.

 


