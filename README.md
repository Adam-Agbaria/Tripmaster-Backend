# SuperApp Project

This project is a comprehensive application designed to manage users, objects, and commands with specific roles and permissions. It includes a PostgreSQL database running in Docker, automatic integration tests using JUnit, and additional enhancements beyond the specified requirements.

## Table of Contents

1. [Project Overview](#project-overview)
2. [Technologies Used](#technologies-used)
3. [Setup and Installation](#setup-and-installation)
4. [Running the Application](#running-the-application)
5. [Running the Tests](#running-the-tests)
6. [Endpoints](#endpoints)




## Project Overview

This application allows users to perform operations based on their roles: ADMIN, SUPERAPP_USER, and MINIAPP_USER. It supports creating, updating, retrieving, and deleting users and objects, as well as executing commands.

## Technologies Used

- Java
- Spring Boot
- PostgreSQL
- Docker
- JUnit
- Retrofit (for client app)
- Python (for flight details service)
- Javascript
- Html

## Setup and Installation

### Prerequisites

- Docker
- Docker Compose
- Java JDK 21
- Gradle
- PostgreSQL

### Clone the Repository

\```bash
git clone https://github.com/your-repo/superapp.git
cd superapp
\```

### Configure Docker

1. **Dockerfile**

\```Dockerfile
FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
\```

2. **docker-compose.yml**

\```yaml
version: '3.8'
services:
  app:
    image: 'docker-spring-boot-postgres:latest'
    build:
      context: .
      dockerfile: Dockerfile
    container_name: app
    depends_on:
      - db
    ports:
      - "8084:8084"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - POSTGRES_USER=user
      - POSTGRES_PASSWORD=password
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/mydb
      - SPRING_DATASOURCE_USERNAME=user
      - SPRING_DATASOURCE_PASSWORD=password
      - SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
      - APP_EXCEPTION_MESSAGE_DETAIL=true
      - SPRING_H2_CONSOLE_ENABLED=true
      - SPRING_H2_CONSOLE_PATH=/h2-console
      - SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=true

  db:
    image: postgres
    container_name: db
    environment:
      - POSTGRES_USER=user
      - POSTGRES_PASSWORD=password
      - POSTGRES_DB=mydb
    ports:
      - "5432:5432"

networks:
  default:
    driver: bridge
\```

### Build and Run the Application

1. **Build the Docker Image**

\```bash
docker-compose build
\```

2. **Run the Application**

\```bash
docker-compose up -d
\```

### Access the Application

- The application will be available at `http://localhost:8084`
- The PostgreSQL database will be available at `localhost:5432`

## Running the Tests

### Integration Tests

To run the integration tests, use the following command:

\```bash
./gradlew test
\```

## Endpoints

### User Endpoints

1. **Create User**
   - **POST /superapp/users**
   - Request body: NewUserBoundary
   - Response: Created user details

2. **Update User**
   - **PUT /superapp/users/{superapp}/{email}**
   - Request body: UserBoundary
   - Response: Updated user details

3. **Get User**
   - **GET /superapp/users/{superapp}/{email}**
   - Response: User details

### Object Endpoints

1. **Create Object**
   - **POST /superapp/objects**
   - Request body: BoundaryObject
   - Response: Created object details

2. **Get Object by ID**
   - **GET /superapp/objects/{superapp}/{id}**
   - Response: Object details

3. **Update Object**
   - **PUT /superapp/objects/{superapp}/{id}**
   - Request body: BoundaryObject
   - Response: Updated object details

4. **Get All Objects with Pagination**
   - **GET /superapp/objects?userSuperapp={superapp}&userEmail={email}&size={size}&page={page}**
   - Response: List of objects

5. **Search Objects by Type**
   - **GET /superapp/objects/search/byType/{type}?userSuperapp={superapp}&userEmail={email}&size={size}&page={page}**
   - Response: List of objects by type

6. **Search Objects by Alias**
   - **GET /superapp/objects/search/byAlias/{alias}?userSuperapp={superapp}&userEmail={email}&size={size}&page={page}**
   - Response: List of objects by alias

7. **Search Objects by Alias Pattern**
   - **GET /superapp/objects/search/byAliasPattern/{pattern}?userSuperapp={superapp}&userEmail={email}&size={size}&page={page}**
   - Response: List of objects by alias pattern

8. **Search Objects by Location**
   - **GET /superapp/objects/search/byLocation/{lat}/{lng}/{distance}?userSuperapp={superapp}&userEmail={email}&units={units}&size={size}&page={page}**
   - Response: List of objects by location

### Command Endpoints

1. **Create Command**
   - **POST /superapp/miniapp/{miniAppName}**
   - Request body: BoundaryCommand
   - Response: Created command details

## Enhancements Beyond Requirements

- Running the Server on Docker (Optional)
- Used EmbeddedId for composite keys in database tables
- Implemented automatic integration tests using JUnit
- Created a client app in Android Studio using Retrofit, updated for Sprint 3 requirements
- Developed an additional server in Python for real-time flight details, avoiding the use of fake data
- Implemented email notifications for successful user registrations
- Excellent implementation of composite keys in tables, earning a bonus

## Project Structure

- `src/main/java/com/example/app/` - Main application code
- `src/test/java/com/example/app/` - Integration tests
- `build.gradle` - Project dependencies and build configuration
- `Dockerfile` - Docker configuration for the application
- `docker-compose.yml` - Docker Compose configuration for the application and PostgreSQL








