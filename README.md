# accenture-test-challenge


### Data Model

The data model is designed in a hierarchical and embedded structure to optimize data access and management:
•	Franchise: Represents a franchise and contains a unique identifier, a name, and a list of branches.
•	Branch: Represents a branch belonging to a franchise. It contains a name and a list of offered products.
•	Product: Represents a product available in a branch. It includes the product’s name and available stock quantity.

All branch and product information is embedded within the franchise document, enabling efficient operations and fast queries in MongoDB.

![img.png](imgs/data_model.png)