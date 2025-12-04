# Java Microservices Assignment

## Objective

Design and implement two Spring Boot microservices—Order Service and Inventory Service—that communicate via REST APIs. The system is designed to be modular and extensible, using the Factory Design Pattern for future expansion.

## Project Setup

### Prerequisites

- Java 17
- Maven

### Environment Configuration

Each service runs on its own port, as defined in their respective `application.properties` files:

-   **Order Service:** `server.port=8080`
-   **Inventory Service:** `server.port=8081`

### Build the Project

Before running the services, build both projects from their respective directories:

```sh
mvn clean install
```

### Running the Services

1.  **Inventory Service:**

    Navigate to the `inventory-service` directory and run the following command:

    ```sh
    mvn spring-boot:run
    ```

2.  **Order Service:**

    Navigate to the `order-service` directory and run the following command:

    ```sh
    mvn spring-boot:run
    ```

## API Documentation

### Inventory Service

-   **GET /inventory/{productId}**

    Returns a list of inventory batches for a given product, sorted by expiry date.

-   **POST /inventory/update**

    Updates inventory after an order is placed. This is typically called by the Order Service.

-   **POST /inventory/product**

    Adds a new product to the inventory.

### Order Service

-   **POST /api/order**

    Places an order and updates inventory accordingly by communicating with the Inventory Service.

-   **GET /api/order/{orderId}**

    Retrieves the details of a specific order by its ID.

-   **GET /api/order/customer/{customerId}**

    Retrieves all orders for a specific customer.

## Testing Instructions

To run the tests for each service, navigate to the respective service's directory (`inventory-service` or `order-service`) and run the following command:

```sh
mvn test
```

### Test Coverage

-   **Unit Tests:** Each service contains unit tests for its services and controllers.
-   **Integration Tests:** The `order-service` includes integration tests that use a mock `InventoryService` to simulate communication between the two services.