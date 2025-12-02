Java Microservices Assignment
Objective
Design and implement two Spring Boot microservices — Order Service and Inventory
Service — that communicate via REST APIs. The system should be modular and extensible
using the Factory Design Pattern, allowing future expansion of services and logic.
Microservice 1: Inventory Service
Requirements:
- Maintain inventory of materials/products.
- Each product can have multiple batches with different expiry dates.
- Implement an endpoint to return inventory batches sorted by expiry date for a given
product.
- Use Spring Data JPA with an H2 in-memory database.
- Implement a Factory Pattern to allow future extension of inventory handling logic.
- Include Controller, Service, and Repository layers.
Endpoints:
GET /inventory/{productId} – Returns list of inventory batches sorted by expiry date.
POST /inventory/update – Updates inventory after an order is placed.
Microservice 2: Order Service
Requirements:
- Accept and process product orders.
- Communicate with Inventory Service to check availability and update stock.
- Use RestTemplate or WebClient for inter-service communication.
- Include Controller, Service, and Repository layers.
- Use Spring Data JPA with H2 database.
Endpoints:
POST /order – Places an order and updates inventory accordingly.

Testing Requirements
- Write unit tests for service logic using JUnit 5 and Mockito.
- Write component/integration tests using @SpringBootTest and H2 database.
- Ensure REST endpoints are covered in tests.
Architecture Requirements
- Follow Factory Design Pattern in Inventory Service to allow future extensibility.
- Ensure all classes are designed to be extendable and loosely coupled.
- Use Lombok to reduce boilerplate code (optional).
- Use Swagger/OpenAPI for API documentation (optional).
Submission Guidelines
- Create a new GitHub repository named: order-inventory-microservices-assignment
- Push both microservices as separate modules or folders within the same repository.
- Include a README.md file with:
• Project setup instructions
• API documentation
• Testing instructions
- Ensure the project builds and runs using Maven or Gradle.