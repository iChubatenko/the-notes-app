# 📝 The Notes App API

A simple and clean RESTful API for managing personal notes — built with **Spring Boot**, **MongoDB**, and **Docker**.  
Includes test coverage for controllers, services, and repositories (with Testcontainers for integration tests).

---

## 🚀 Features

- Create, update, delete, and retrieve notes
- Filter notes by tags
- MongoDB persistence layer
- Automatic Swagger UI documentation
- Integration tests with **Testcontainers**
- Ready for containerized deployment via **Docker Compose**

---
## 🧑‍💻 Running the Application

### ▶️ Start with Docker Compose

Run the entire stack (Spring Boot + MongoDB) using:

>docker-compose up --build
 
This will:
- Build the Spring Boot app
- Start MongoDB in a container
- Expose the API on http://localhost:8080

To stop and remove all containers:
>docker-compose down

---
## 📘 API Documentation

After running the app, visit the Swagger UI at:

👉 http://localhost:8080/swagger-ui.html￼

You’ll see all available endpoints and can test them directly from your browser.