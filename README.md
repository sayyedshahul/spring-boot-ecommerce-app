# Spring Boot eCommerce Application

This is a full-featured **Spring Boot eCommerce backend application**.  
It demonstrates hands-on experience with **REST API design, Spring Data JPA, JWT authentication, role-based authorization, Redis Caching**, and clean application architecture with **DTOs and service layers**.  
Built to showcase practical backend development skills suitable for real-world applications.

---

## 🚀 Features

- **User Management**
    - Register and login users (roles: USER, SELLER, ADMIN)
    - Role-based access control (RBAC)

- **Product Management**
    - CRUD operations for products (ADMIN/SELLER)
    - Product listing with pagination and sorting

- **Cart & Orders**
    - Add/remove/update product quantities in cart
    - Place orders with payment details
    - Seller-specific order view

- **Security**
    - JWT-based authentication
    - Stateless session management
    - Secure endpoints based on roles

- **Database & Persistence**
    - Spring Data JPA with MySQL / H2
    - Entities, DTOs, and service layer separation
    - Exception handling and validations

- **Redis Caching:**  
  Implemented Redis caching for frequently accessed endpoints:
    - **Product listings (`product-list`)** – caches first pages of product lists with pagination and sorting, TTL 5 minutes.
    - **Product search (`product-search`)** – caches hot search keywords (e.g., iPhone, Samsung) with TTL 1 minute to keep results fresh.
    - **Category listings (`category-list`)** – caches frequently accessed category pages with TTL 10 minute.
    - **Eviction strategy:** All list/search caches are automatically evicted on product creation, update, deletion, or image update.
    - **Benefits:** Reduced database load, faster API response times, and efficient handling of frequently requested data.

- **Swagger Integration**
    - API documentation via OpenAPI/Swagger UI
    - JWT authentication support in Swagger

---

## 🛠 Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Data JPA / Hibernate
- MySQL / H2 Database
- Spring Security with JWT
- Redis
- ModelMapper
- Swagger / OpenAPI

---

## 📦 Setup Instructions

1. **Clone the repository**
      ```bash
      git clone <repo-url>
      cd <repo-folder>
      ```

2. **Configure environment**
   - Copy application-example.properties → application-local.properties and set database credentials and JWT secret.

3. **Run the application**
    ```bash
    ./mvnw spring-boot:run
    ```
4. **Access API docs**
   - Swagger UI at http://localhost:8080/swagger-ui/index.html


## Design Decisions

- **Layered Architecture:** The project uses a clear separation of concerns with **Controller → Service → Repository → Entity/DTO** layers to make the code maintainable and testable.
- **DTOs for Data Transfer:** All responses use **DTOs** to avoid exposing internal entity structures directly to the clients.
- **Spring Data JPA:** Used for database interactions to leverage repository abstractions and simplify CRUD operations.
- **JWT-based Authentication:** Stateless security using **JWT tokens** with role-based authorization to support multiple user roles (USER, SELLER, ADMIN).
- **Exception Handling:** Global exception handling via **custom exceptions and `@ControllerAdvice`** for consistent API error responses.
- **Swagger Integration:** OpenAPI/Swagger for automatic API documentation and easier testing.
- **Pagination and Sorting:** Implemented for product listings and order history to handle large datasets efficiently.
- **CommandLineRunner Initialization:** Roles and default users are initialized on application startup to simplify testing and demoing.
- **Redis Caching:** Implemented for product and category data to enable faster API response times, handle frequently access datasets efficiently  and, reduce load on the database.


## Future Improvements

- Add **payment gateway integration** for transactions.
- Implement **unit and integration tests** for services and controllers.
- Containerize the application using **Docker** for easier deployment.
- Integrate **email notifications** for order confirmation and status updates.

## Postman Collection

- Import the Postman collection from:
/postman/ecommerce-api.postman_collection.json
