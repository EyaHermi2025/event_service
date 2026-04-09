#  Event Service - English School Platform

##  Overview

The **Event Service** is a microservice developed using **Spring Boot** as part of the *English School Platform*.
It is responsible for managing all event-related operations such as creation, registration, participation tracking, and analytics.

---

##  Features

### Event Management

* Create, update, delete events
* Manage event details (title, description, date, location, capacity)
* Support for different event types (workshops, conferences, social events, etc.)

###  Registration System

* User registration for events

### Advanced Statistics

* Participation rate
* Attendance tracking (check-in system)
* User profile analytics (age, department, interests)
* Event popularity and engagement metrics

###  Notifications 
* Real-time updates (WebSocket support)

### QR Code Check-in 

* Unique QR code for each participant
* Fast check-in during events

---

## 🛠️ Technologies Used

* Java 17
* Spring Boot
* Spring Data JPA
* MySQL
* Maven
* REST APIs

---

## 📂 Project Structure

```bash
src/
 ├── controller/
 ├── service/
 ├── repository/
 ├── entity/
 └── dto/
```

---

## ⚙️ Installation & Setup

### 1️⃣ Clone the repository

```bash
git clone https://github.com/EyaHermi2025/event_service.git
```

### 2️⃣ Navigate to the project

```bash
cd event_service
```

### 3️⃣ Configure the database

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_db
spring.datasource.username=root
spring.datasource.password=
```

### 4️⃣ Run the application

```bash
mvn spring-boot:run


 Example Statistics

* Total registrations
* Attendance rate
* Most popular events
* User engagement metrics

---

 Author

Developed by **Eya Hermi**


