# Task Manager Application (Backend)

This is the backend service for the Task Tracker application, built with Java and Spring Boot. It provides a RESTful API for task management, user authentication, and Role-Based Access Control (RBAC), alongside real-time updates using WebSockets.

## Architecture Overview & Design Decisions
- **Layered Architecture:** The application follows a strict Controller-Service-Repository pattern. This separation of concerns ensures that business logic is isolated in the Service layer, making it easier to test and maintain.
- **Security:** Spring Security is used with JWT (JSON Web Tokens) for stateless authentication. Passwords are securely hashed using BCrypt.
- **Role-Based Access Control (RBAC):** We implemented method-level security using `@PreAuthorize` to ensure that administrative endpoints (like deleting users or changing roles) are strictly limited to the `ROLE_ADMIN` authority.
- **Real-Time WebSockets:** STOMP over WebSockets is used to push real-time task updates to all connected clients instantly when a task is created, modified, or deleted.

## Setup Instructions

### Prerequisites
- **Java:** JDK 21
- **Maven:** 3.8+ (to build and run)
- **Database:** MySQL Server (Running on port 3306)

### Database Configuration
The application uses MySQL. By default, it expects a database named `task_manager` running on `localhost:3306`. It will automatically create the database if it does not exist.

You can override the default credentials by setting the following Environment Variables before running the application:
- `DB_URL` (Default: `jdbc:mysql://localhost:3306/task_manager?createDatabaseIfNotExist=true`)
- `DB_USERNAME` (Default: `root`)
- `DB_PASSWORD` (Default: `1234@Janith`)

### Running the Application

1. **Clone the repository and navigate to the project directory:**
   ```bash
   cd Task_Manager
   ```

2. **Run the Spring Boot application:**
   Using Maven:
   ```bash
   mvn spring-boot:run
   ```
   Or run it directly from your IDE (e.g., IntelliJ IDEA) by executing the `Main.java` class.

3. The server will start on `http://localhost:8080`.

### Running Tests
The project contains a comprehensive suite of automated tests covering the core business logic (Service Layer) using JUnit 5 and Mockito.
To execute the tests, run:
```bash
mvn test
```

## Assumptions & Future Improvements
- **Assumptions:** It is assumed that the frontend application is running on `http://localhost:4200`, as the CORS configuration is hardcoded to permit traffic from this origin. It is also assumed that administrators are manually assigned their roles by database seeding or manual intervention, as public registration defaults to `ROLE_USER`.
- **Future Improvements:** 
  - Add Docker support (Dockerfile & docker-compose.yml) to containerize the database and application together.
  - Implement a caching layer (e.g., Redis) to optimize frequent reads for task lists.
  - Add pagination and advanced filtering to the `getAllTasks` endpoint to handle large datasets more efficiently.
