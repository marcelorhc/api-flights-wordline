
# Flights API

A Spring Boot application for managing data from flights.

I assumed some requirements that was not described into the assignment, we can discuss more in the next round.

The given API from crazy suppliers does not exist, so locally I used a mock server (Mockoon) to retrieve the information.

## Features to add in the future

- Security check
- Since I did not know the requirements for model, add into the mapping
- Add a database (Remove in memory H2)

## Requirements

- **Java 21**
- **Docker** (optional, for containerized deployment)
- **Maven** (for local build, if not using Docker)

## Technologies Used

- **Spring Boot**
- **Java 21**
- **Feign**
- **Wiremock**
- **Swagger**
- **H2 Database**
- **Docker**

## Running the Application Locally

### Step 1: Clone the Repository

```bash
git clone https://github.com/marcelorhc/api-flights-wordline.git
cd api-flights-wordline
```

### Step 2: Build the Project with Maven

If you don’t have Docker installed and prefer to run the application locally:

```bash
mvn clean install
mvn spring-boot:run
```

By default, the application will run on **`http://localhost:8080`**.

---

## Dockerizing the Application

### Step 1: Build the Docker Image



```bash
docker build -t flights-api .
```

### Step 2: Run the Docker Container

```bash
docker run -p 8080:8080 flights-api
```

This will start the application and map port `8080` on the host machine to the container’s port `8080`.

---

## Accessing Swagger UI

Once the application is running, you can access the **Swagger UI** for exploring and testing the API:

- Open your browser and navigate to `http://localhost:8080/swagger-ui.html`
