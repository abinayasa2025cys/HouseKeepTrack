# 🏨 HouseKeepTrack
## Hotel Housekeeping Task Assignment by Room Status

A complete **Spring Boot 3 + JPA + MySQL REST API** project for managing hotel housekeeping operations, with a simple web dashboard for demonstration.

---

## ✨ Features

| Feature | Description |
|---|---|
| Room Status Tracking | Full lifecycle: DIRTY → CLEANING → CLEANED → INSPECTED → READY |
| Auto Task Creation | When a room becomes DIRTY, a cleaning task is instantly created |
| Auto Housekeeper Assignment | Available housekeeper is automatically assigned and set to BUSY |
| Cleaning Workflow | Tasks go: ASSIGNED → IN_PROGRESS → COMPLETED |
| Supervisor Inspection | PASS → room becomes READY | FAIL → new cleaning task |
| Failed Inspection Workflow | Failed inspection sends room back to CLEANING with new task |
| Guest Allocation Validation | Only READY rooms can be allocated to guests |
| Housekeeper Workload | View assigned/in-progress/completed task counts per housekeeper |
| Average Turnaround Time | Calculated from startedAt → completedAt using Duration API |
| Full CRUD APIs | POST, GET, PUT, DELETE for all resources |
| Swagger/OpenAPI | Complete API documentation with interactive testing |
| Simple Web UI | Dashboard, rooms, housekeepers, tasks, and inspections |
| MySQL Persistence | All data persisted with proper JPA relationships |

---

## 🛠️ Technologies

| Technology | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring Web MVC | — |
| Spring Data JPA | — |
| Jakarta Bean Validation | — |
| MySQL | 8.x |
| Maven | 3.x |
| Swagger / OpenAPI | springdoc-openapi 2.3.0 |
| Lombok | — |
| HTML + CSS + Vanilla JS | — |

---

## 🔧 How to Run

### 1. Prerequisites

- Java 17 installed
- MySQL 8.x running on `localhost:3306`
- Maven 3.x installed
- MySQL credentials: `root / Root` (or update `application.properties`)

### 2. Database Setup

The database is created automatically on startup via:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/housekeep_track_db?createDatabaseIfNotExist=true
spring.jpa.hibernate.ddl-auto=update
```

No manual SQL scripts are needed.

### 3. Configure application.properties

Edit `src/main/resources/application.properties` if your MySQL credentials differ:

```properties
spring.datasource.username=root
spring.datasource.password=Root
server.port=8081
```

### 4. Build the Project

```bash
cd HouseKeepTrack
mvn clean package -DskipTests
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR:

```bash
java -jar target/housekeeptrack-0.0.1-SNAPSHOT.jar
```

### 6. Access the Application

| URL | Description |
|---|---|
| `http://localhost:8081/` | Web Dashboard |
| `http://localhost:8081/swagger-ui/index.html` | Swagger API Documentation |
| `http://localhost:8081/api-docs` | OpenAPI JSON |

---

## 📐 Project Structure

```
HouseKeepTrack/
├── pom.xml
└── src/main/java/com/example/housekeeptrack/
    ├── HouseKeepTrackApplication.java
    ├── SwaggerConfig.java
    ├── WebConfig.java
    ├── model/
    │   ├── Room.java
    │   ├── CleaningTask.java
    │   ├── Housekeeper.java
    │   ├── Inspection.java
    │   └── enums/
    │       ├── RoomStatus.java        (DIRTY,CLEANING,CLEANED,INSPECTED,READY)
    │       ├── HousekeeperStatus.java (AVAILABLE,BUSY)
    │       ├── TaskStatus.java        (ASSIGNED,IN_PROGRESS,COMPLETED)
    │       └── InspectionResult.java  (PASSED,FAILED)
    ├── repository/
    │   ├── RoomRepository.java
    │   ├── CleaningTaskRepository.java
    │   ├── HousekeeperRepository.java
    │   └── InspectionRepository.java
    ├── dto/
    │   ├── request/
    │   │   ├── RoomRequest.java
    │   │   ├── RoomStatusRequest.java
    │   │   ├── HousekeeperRequest.java
    │   │   └── InspectionRequest.java
    │   └── response/
    │       ├── RoomResponse.java
    │       ├── CleaningTaskResponse.java
    │       ├── HousekeeperResponse.java
    │       ├── HousekeeperWorkloadResponse.java
    │       └── InspectionResponse.java
    ├── service/
    │   ├── RoomService.java
    │   ├── CleaningTaskService.java
    │   ├── HousekeeperService.java
    │   └── InspectionService.java
    ├── controller/
    │   ├── RoomController.java
    │   ├── CleaningTaskController.java
    │   ├── HousekeeperController.java
    │   └── InspectionController.java
    └── exception/
        ├── ResourceNotFoundException.java
        ├── BusinessRuleException.java
        ├── InvalidStateException.java
        └── GlobalExceptionHandler.java
```

---

## 🌐 API Documentation

### Room APIs

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/rooms` | Create a new room |
| `GET` | `/api/rooms` | Get all rooms |
| `GET` | `/api/rooms/{id}` | Get room by ID |
| `PUT` | `/api/rooms/{id}/status` | Update room status |
| `PUT` | `/api/rooms/{id}/allocate` | Allocate room to guest (READY only) |
| `DELETE` | `/api/rooms/{id}` | Delete room (no active tasks) |
| `GET` | `/api/rooms/statistics` | Room count by status |
| `GET` | `/api/rooms/turnaround/average` | Avg cleaning turnaround time |

### Housekeeper APIs

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/housekeepers` | Register housekeeper |
| `GET` | `/api/housekeepers` | Get all housekeepers |
| `GET` | `/api/housekeepers/{id}` | Get housekeeper by ID |
| `GET` | `/api/housekeepers/workload` | View workload per housekeeper |
| `PUT` | `/api/housekeepers/{id}` | Update housekeeper info |
| `DELETE` | `/api/housekeepers/{id}` | Delete housekeeper (no active tasks) |

### Cleaning Task APIs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/cleaning-tasks` | Get all tasks |
| `GET` | `/api/cleaning-tasks/{id}` | Get task by ID |
| `PUT` | `/api/cleaning-tasks/{id}/start` | Start an ASSIGNED task |
| `PUT` | `/api/cleaning-tasks/{id}/complete` | Complete an IN_PROGRESS task |

### Inspection APIs

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/rooms/{roomId}/inspections` | Submit inspection |
| `GET` | `/api/rooms/{roomId}/inspections` | Get inspection history |

---

## 🔄 Room Status Lifecycle

```
Guest Checks Out
      ↓
Room → DIRTY
      ↓
Auto: Find AVAILABLE Housekeeper
      ↓
Create CleaningTask → Housekeeper = BUSY
      ↓
Room → CLEANING
      ↓
Start Task (IN_PROGRESS)
      ↓
Complete Task (COMPLETED)
      ↓
Room → CLEANED → Housekeeper = AVAILABLE
      ↓
Supervisor Inspection
      ↓
   PASSED          FAILED
     ↓               ↓
 INSPECTED        CLEANING (new task)
     ↓
  READY
     ↓
Guest Allocation ✅
```

### Valid Status Transitions

| From | To | Trigger |
|---|---|---|
| DIRTY | CLEANING | Auto when housekeeper assigned |
| CLEANING | CLEANED | Task completion |
| CLEANED | INSPECTED | Passed inspection |
| INSPECTED | READY | Auto after inspection passes |
| CLEANED | CLEANING | Failed inspection (auto) |

### Invalid Transitions (rejected with 400)

```
DIRTY → READY ❌
DIRTY → INSPECTED ❌
CLEANED → READY (without inspection) ❌
READY → CLEANING ❌
```

---

## 🏗️ Database Relationships

```
ROOM ─────────────── 1 : N ─── CLEANING_TASK
ROOM ─────────────── 1 : N ─── INSPECTION
HOUSEKEEPER ───────── 1 : N ─── CLEANING_TASK
```

---

## ⚙️ Business Rules

1. **Automatic Task Creation**: When a room becomes `DIRTY`, the system auto-creates a `CleaningTask` and assigns the first `AVAILABLE` active housekeeper.
2. **No Available Housekeeper**: If no housekeeper is available, the task is created without assignment; room stays `DIRTY`.
3. **READY requires Inspection**: `CLEANED → READY` transition is blocked. Must pass inspection first.
4. **Failed Inspection**: Remarks are mandatory. Room reverts to `CLEANING` with a new task.
5. **Delete Protection**: Cannot delete a room or housekeeper if they have an active task.
6. **Guest Allocation**: Only `READY` rooms can be allocated to guests.

---

## 🎓 Project Review Notes

This project demonstrates:
- **4-layer architecture**: Controller → Service → Repository → Database
- **JPA relationships**: `@OneToMany`, `@ManyToOne`, `@JoinColumn`
- **Enum-based state machine** with validation
- **Global exception handling** with structured JSON errors
- **DTOs** separating API from domain model
- **Bean Validation** on request DTOs
- **Swagger/OpenAPI** with operation descriptions
- **Simple HTML/JS UI** served by Spring Boot static resources
"# HouseKeepTrack" 
