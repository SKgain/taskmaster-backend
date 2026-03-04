# 🗂️ TaskMaster — Enterprise Task Management System

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=flat-square&logo=docker)
![JWT](https://img.shields.io/badge/Auth-JWT-black?style=flat-square&logo=jsonwebtokens)
![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini-4285F4?style=flat-square&logo=google)
![WebSocket](https://img.shields.io/badge/RealTime-WebSocket-yellow?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

A production-grade, full-featured **task and team management REST API** built with Spring Boot. Designed with clean architecture, role-based access control, real-time communication, AI-powered assistance, and full Docker support — built to scale from startup to enterprise.

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [API Modules](#-api-modules)
- [Security Model](#-security-model)
- [AI Integration](#-ai-integration)
- [Real-Time Chat](#-real-time-chat)
- [Email Notifications](#-email-notifications)
- [Profiles & Configuration](#-profiles--configuration)
- [Docker Deployment](#-docker-deployment)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Logging](#-logging)

---

## 🧩 Overview

TaskMaster is a robust backend system for managing teams, projects, tasks, and meetings. It supports three user roles — **Admin**, **Manager**, and **Member** — each with distinct permissions enforced at the method level using Spring Security.

Built with **developer experience and production-readiness in mind**: environment-separated configs, structured logging, containerized deployment, and Swagger docs out of the box.

---

## ✨ Features

| Category | Capability |
|---|---|
| 👤 Auth | JWT authentication, refresh tokens, email verification, forgot/reset password |
| 🛡️ Security | Role-based access control (ADMIN / MANAGER / MEMBER) |
| 📋 Tasks | Full CRUD, assignment, reassignment, status tracking, overdue detection, activity log |
| 📁 Projects | Create, archive, delete, member management, statistics |
| 📅 Meetings | Schedule, cancel, complete, participant management |
| 💬 Chat | Real-time WebSocket messaging with typing indicators and message history |
| 🤖 AI | Google Gemini-powered task description generation |
| 📊 Analytics | Manager dashboards, project progress, priority analysis, deadline tracking |
| 📧 Email | SMTP-based transactional emails via Gmail |
| 🐳 Docker | Multi-service Docker Compose with PostgreSQL |
| 🔍 Logging | Structured console + file logging, profile-aware verbosity, AOP-based `@ApiLog` tracing |
| 📖 Docs | Auto-generated Swagger/OpenAPI UI |

---

## 🛠️ Tech Stack

**Core**
- Java 21, Spring Boot 3.x
- Spring Web, Spring Security, Spring Data JPA
- PostgreSQL 15
- Hibernate ORM
- JPQL (custom `@Query` for complex bulk operations and filtered lookups)

**Infrastructure**
- Docker & Docker Compose
- Eclipse Temurin 21 (Alpine) base image

**Auth & Security**
- JWT (JSON Web Tokens) with refresh token support
- Spring Security method-level authorization (`@PreAuthorize`)
- BCrypt password encoding

**Real-Time**
- Spring WebSocket + STOMP
- SimpMessagingTemplate for topic broadcasting

**AI**
- Google Gemini 2.5 Flash via REST API

**Email**
- Spring Mail + Gmail SMTP (STARTTLS)

**Developer Experience**
- Lombok — boilerplate-free models and services
- SLF4J + Logback — structured, profile-aware logging
- Spring AOP — custom `@ApiLog` annotation for declarative request/response tracing
- SpringDoc OpenAPI (Swagger UI)
- Dev/Prod Spring profiles
- Jakarta Bean Validation

---

## 🏗️ Architecture

The codebase follows a strict **layered architecture** (Controller → Service → Repository) with clean domain separation across five top-level packages.

```
com.dhrubok.taskmaster/
│
├── auth/                               # Security & Authentication
│   ├── configs/
│   │   └── SecurityConfig              # Spring Security filter chain, CORS, CSRF, session policy
│   ├── constants/
│   │   └── SecurityConstant            # JWT secret, token TTLs, header names
│   ├── filters/
│   │   └── JwtFilter                   # OncePerRequestFilter — validates JWT on every request
│   ├── principles/
│   │   └── UserDetailsPrinciple        # Wraps User entity as Spring UserDetails
│   └── services/
│       ├── CustomUserDetailsService    # Loads user from DB for Spring Security
│       ├── JWTService                  # Token generation, validation, claims extraction
│       └── UserService                 # Sign-up, sign-in, verify, refresh, logout, password flows
│
├── common/                             # Cross-Cutting Concerns
│   ├── annotations/
│   │   └── ApiLog                      # Custom method annotation — marks endpoints for AOP logging
│   ├── aspect/
│   │   └── LoggingAspect               # @Around advice — logs HTTP method, path, args, response, exec time
│   ├── configs/
│   │   ├── AppConfig                   # ModelMapper, RestTemplate beans
│   │   ├── AuditingConfig              # JPA Auditing — auto-sets createdBy / updatedBy
│   │   ├── MailConfig                  # JavaMailSender bean (Gmail SMTP / STARTTLS)
│   │   ├── OpenApiConfig               # Swagger UI + JWT security scheme
│   │   ├── WebConfig                   # CORS registry, static resource handler (profile images)
│   │   └── WebSocketConfig             # STOMP broker, JWT handshake interceptor (highest precedence)
│   ├── constants/
│   │   ├── ErrorCode                   # Centralised error message constants
│   │   └── SuccessCode                 # Centralised success message constants
│   ├── entities/
│   │   └── AuditModel                  # @MappedSuperclass — createdAt, updatedAt, createdBy, updatedBy
│   ├── exceptions/
│   │   ├── ApplicationException        # Generic runtime exception
│   │   ├── DuplicateResourceException  # 409 Conflict scenarios
│   │   ├── ResourceNotFoundException   # 404 Not Found scenarios
│   │   ├── UnauthorizedException       # 403 Forbidden scenarios
│   │   └── GlobalExceptionHandler      # @RestControllerAdvice — unified error response shape
│   ├── models/
│   │   └── Response                    # Unified API response wrapper {success, message, data}
│   └── services/
│       └── EmailService                # HTML template-based transactional emails (6 types)
│
├── core/                               # API Layer — Controllers only, no business logic
│   └── controllers/
│       ├── admin/
│       │   ├── AdminController         # Stats, health, user management, broadcast notifications
│       │   ├── AdminConfigController   # System config CRUD + reset to defaults
│       │   └── AuthController          # Public auth endpoints (sign-up, sign-in, verify, etc.)
│       └── features/
│           ├── ai/
│           │   └── AiController        # POST /ai/generate — Gemini task description
│           ├── chat/
│           │   └── ChatController      # REST history + WebSocket message/typing handlers
│           ├── meeting/
│           │   └── MeetingController   # Full meeting lifecycle (Manager-gated operations)
│           ├── project/
│           │   └── ProjectController   # Project CRUD, member management, stats
│           ├── task/
│           │   └── TaskController      # Task CRUD, assignment, status, activity log
│           └── user/
│               ├── UserController          # Profile, photo upload, password change, search
│               ├── ManagerController       # Member creation and lifecycle management
│               ├── ManagerAnalyticsController  # Dashboard, progress, priority, deadlines
│               └── UserSettingsController  # Notification and preference settings
│
├── fileobject/                         # File Storage
│   └── services/
│       └── FileStorageService          # Local disk storage — validate, store, delete profile images
│
└── persistence/                        # Data Layer — Entities, Repositories, Services, Models
    ├── auth/
    │   ├── entities/User               # Core user entity (UUID PK, roles, verification, tokens)
    │   ├── enums/RoleType              # ADMIN | MANAGER | MEMBER
    │   ├── models/                     # SignUpRequest, SignInRequest, AuthUserResponse, etc.
    │   └── repositories/UserRepository # 20+ query methods incl. custom JPQL @Query for bulk broadcast updates
    │
    ├── system/
    │   ├── entities/SystemConfig       # Global platform settings (registration, 2FA, timeouts)
    │   ├── models/SystemConfigDTO      # Validated DTO with @Min/@Max constraints
    │   ├── repositories/SystemConfigRepository
    │   └── services/SystemConfigService
    │
    └── features/
        ├── admin/
        │   ├── models/                 # SystemStatsResponse, SystemHealthResponse, ProjectSummary
        │   └── services/AdminService   # Aggregate stats, user promotion/demotion, broadcast
        │
        ├── ai/
        │   ├── models/                 # GeminiRequest, GeminiResponse (REST API DTOs)
        │   └── services/AiTaskService  # Calls Gemini 2.5 Flash via RestTemplate
        │
        ├── chat/
        │   ├── entities/ChatMessage    # Indexed on (projectId, timestamp)
        │   ├── repositories/ChatMessageRepository
        │   └── services/ChatService    # Project membership guard, edit/delete ownership check
        │
        ├── meeting/
        │   ├── entities/Meeting        # Extends AuditModel, ManyToMany participants
        │   ├── enums/MeetingStatus     # SCHEDULED | IN_PROGRESS | COMPLETED | CANCELLED
        │   ├── models/                 # CreateMeetingRequest, UpdateMeetingRequest, MeetingResponse
        │   ├── repositories/MeetingRepository
        │   └── services/MeetingService
        │
        ├── project/
        │   ├── entities/Project        # Extends AuditModel
        │   ├── enums/ProjectStatus
        │   ├── models/                 # CreateProjectRequest, ProjectResponse, ProjectProgressResponse
        │   ├── repositories/ProjectRepository
        │   └── services/ProjectService
        │
        ├── projectmember/
        │   ├── entities/ProjectMember  # Join table with ProjectRole (OWNER | ADMIN | MEMBER)
        │   ├── enums/ProjectRole
        │   ├── models/                 # MemberAvailabilityResponse, MemberPerformanceResponse
        │   └── repositories/ProjectMemberRepository
        │
        ├── task/
        │   ├── entities/               # Task (extends AuditModel), TaskActivity
        │   ├── enums/                  # TaskStatus, TaskPriority, ActivityType
        │   ├── models/                 # CreateTaskRequest, UpdateTaskRequest, TaskActivityResponse
        │   ├── repositories/           # TaskRepository, TaskActivityRepository
        │   └── services/               # TaskService, TaskActivityService
        │
        └── user/
            ├── entities/UserSettings   # Per-user notification and UI preferences
            ├── models/                 # UserResponse, UpdateProfileRequest, CreateMemberRequest
            ├── repositories/UserSettingsRepository
            └── services/
                ├── UserUserService         # Profile, photo, password, active user queries
                ├── MemberManagementService # Manager-scoped member CRUD + verification
                ├── ManagerAnalyticsService # Dashboard aggregation, workload, deadline analysis
                └── UserSettingsService     # Settings CRUD with defaults
```

---

## 📡 API Modules

### 🔐 Auth — `/api/auth`
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/sign-up` | Register new user |
| GET | `/verify` | Email verification via token |
| POST | `/sign-in` | Login, receive JWT + refresh token |
| POST | `/refresh-token` | Rotate access token |
| POST | `/logout` | Invalidate session |
| POST | `/forgot-password` | Send reset email |
| POST | `/reset-password` | Set new password |
| POST | `/resend-verification` | Resend verification email |

### 📋 Tasks — `/api/tasks`
Full CRUD with role-aware access. Members can update status on their own tasks; Managers control everything.

`GET /tasks` · `POST /tasks` · `PUT /tasks/{id}` · `DELETE /tasks/{id}` · `PATCH /tasks/{id}/status` · `PATCH /tasks/{id}/assign/{memberId}` · `GET /tasks/my-tasks` · `GET /tasks/overdue` · `GET /tasks/stats` · `GET /tasks/search` · `GET /tasks/{id}/activities`

### 📁 Projects — `/api/projects`
`GET /projects` · `POST /projects` · `PUT /projects/{id}` · `DELETE /projects/{id}` · `PATCH /projects/{id}/archive` · `POST /projects/{id}/members/{memberId}` · `DELETE /projects/{id}/members/{memberId}` · `GET /projects/{id}/stats`

### 📅 Meetings — `/api/meetings`
`GET /meetings` · `POST /meetings` · `PUT /meetings/{id}` · `PATCH /meetings/{id}/cancel` · `PATCH /meetings/{id}/complete` · `DELETE /meetings/{id}` · `POST /meetings/{id}/participants/{userId}` · `GET /meetings/upcoming` · `GET /meetings/past` · `GET /meetings/stats`

### 💬 Chat — `/api/chat` + WebSocket
- `GET /chat/history/{projectId}` — Paginated message history
- `PUT /chat/messages/{id}` — Edit message (own only)
- `DELETE /chat/messages/{id}` — Delete message (own or Manager)
- WebSocket `/chat.sendMessage` — Send real-time message to project topic
- WebSocket `/chat.typing` — Broadcast typing indicator

### 📊 Manager Analytics — `/api/manager/analytics`
`GET /dashboard` · `GET /projects/progress` · `GET /tasks/priority-analysis` · `GET /tasks/upcoming-deadlines`

### 🤖 AI — `/api/ai`
`POST /ai/generate` — Generate task description from a title using Gemini 2.5 Flash

### 👑 Admin — `/api/admin`
System stats, health check, user management (promote/demote/activate/deactivate), broadcast notifications, system config CRUD.

---

## 🔒 Security Model

```
ADMIN
  └── Full system access: user management, system config, broadcast notifications

MANAGER
  ├── Full project & task management
  ├── Create/manage members, meetings
  ├── Analytics dashboard
  └── Chat moderation (delete any message)

MEMBER
  ├── View assigned tasks & projects
  ├── Update own task status
  └── Chat within project (edit/delete own messages)
```

Authentication uses **stateless JWT** with a separate refresh token flow. Tokens are validated via a custom filter injected into the Spring Security filter chain. Method-level security is enforced with `@PreAuthorize("hasRole('...')")`.

---

## 🤖 AI Integration

TaskMaster integrates **Google Gemini 2.5 Flash** to assist managers with task creation. By providing a task title, the AI generates a detailed, context-aware task description — reducing manual effort and improving task clarity across teams.

```http
POST /api/ai/generate
{ "title": "Set up CI/CD pipeline" }
```

```json
{
  "success": true,
  "message": "Suggestion generated",
  "data": {
    "suggestion": "Configure a GitHub Actions workflow to automate build, test, and deployment stages..."
  }
}
```

---

## 💬 Real-Time Chat

Built on **Spring WebSocket + STOMP**. Each project has its own message topic. Clients subscribe to `/topic/project/{projectId}` to receive live messages, edits, deletes, and typing indicators.

WebSocket connection requires JWT-authenticated handshake. Sender project membership is validated server-side before any message is persisted or broadcast.

---

## 📧 Email Notifications

Transactional emails are sent via **Gmail SMTP** (STARTTLS on port 587) using Spring Mail. Triggered events include:

- Account verification on sign-up or member creation
- Forgot password / reset password
- Broadcast notifications from Admin to all or role-filtered users

---

## ⚙️ Profiles & Configuration

TaskMaster uses **Spring Profile separation** to keep dev and prod environments cleanly isolated.

**`dev` profile** (`application-dev.properties`)
- Verbose SQL logging, DEBUG level for app packages
- `ddl-auto=update` for fast iteration
- Local datasource URL

**`prod` profile** (`application-prod.properties`)
- All sensitive values injected via environment variables (`${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}`)
- `ddl-auto=none` — no schema changes on deploy
- Minimal logging, structured console + file output to `logs/taskmaster.log`

```properties
# Switch profiles in application.properties
spring.profiles.active=dev   # or prod
```

---

## 🐳 Docker Deployment

The entire stack (app + database) runs with a single command.

```bash
docker-compose up --build
```

**`docker-compose.yml`** spins up:
- `task-db` — PostgreSQL 15 with persistent volume
- `task-backend` — Spring Boot app, auto-connects to DB via service name

**`Dockerfile`**
```dockerfile
FROM eclipse-temurin:21-jdk-alpine
COPY target/taskmaster-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

For production, override datasource credentials via Docker environment variables — no credentials in the image.

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL (if running locally without Docker)

### Local Development

```bash
# Clone the repository
git clone https://github.com/yourusername/taskmaster.git
cd taskmaster

# Build the project
mvn clean package -DskipTests

# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker (Recommended)

```bash
mvn clean package -DskipTests
docker-compose up --build
```

App available at `http://localhost:8080`

---

## 📖 API Documentation

Swagger UI is auto-generated via SpringDoc OpenAPI and available at:

```
http://localhost:8080/swagger-ui.html
```

All secured endpoints display the JWT lock icon. Authenticate once via `/api/auth/sign-in`, paste your token using the **Authorize** button, and explore the full API interactively.

---

## 📋 Logging

| Profile | Level | Output |
|---------|-------|--------|
| `dev` | DEBUG (app), SQL trace | Console |
| `prod` | INFO (app), WARN (infra) | Console + `logs/taskmaster.log` |

Logs follow the pattern:
```
2025-01-15 14:32:01 [http-nio-8080-exec-1] INFO  c.d.t.TaskController - Task created: task-123
```

SLF4J with `@Slf4j` (Lombok) is used consistently across all controllers and services.

### 🎯 AOP Request Tracing — `@ApiLog`

A custom `@ApiLog` annotation powered by **Spring AOP** provides structured per-request tracing on any controller method:

```java
    @Operation(summary = "Create a new task (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiLog
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<Response> createTask(@Valid @RequestBody CreateTaskRequest request) { ... }
```

Each annotated call automatically logs:

- Timestamp, HTTP method, API path, method name, description
- Serialized request parameters and body (non-primitive args)
- Execution time in milliseconds
- Response payload or error message on failure

```
========== API CALL START ==========
Timestamp: 2025-01-15T14:32:01.123
HTTP Method: POST
API Path: []
Method Name: createTask
Description: Create new task
Request Body: {"title":"Fix login bug","projectId":"proj-456"}
Execution Time: 47 ms
Response Status: SUCCESS
========== API CALL END ==========
```

This is opt-in per method via `@ApiLog`, keeping noise low while giving full visibility where it matters.

---

## 👨‍💻 Author

Built by **Saikat Kumar Gain** — a backend-focused Java developer passionate about clean architecture, scalable APIs, and developer-friendly codebases.

> 📬 Open to full-time backend / Java / Spring Boot roles.
