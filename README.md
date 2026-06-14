# Todo Backend — Project Reference

Two independent Spring Boot microservices in one repository.  
Walk through each concept in order using the file links below — every link opens the actual source file.

**GitHub Repository:** https://github.com/ragav101/todo-app-cicd.git

---

## Repository & Branching

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code only — always stable and deployable |
| `develop` | Active development — all features and fixes merge here first |

**Clone the repository:**
```bash
git clone https://github.com/ragav101/todo-app-cicd.git
cd todo-app-cicd
```

**Branch workflow:**
```bash
# Start a new feature from develop
git checkout develop
git checkout -b feature/your-feature-name

# Merge completed feature back into develop
git checkout develop
git merge feature/your-feature-name
git push origin develop

# When develop is stable, merge into main for release
git checkout main
git merge develop
git push origin main
```

**Branch naming conventions:**

| Prefix | Purpose | Example |
|--------|---------|---------|
| `feature/*` | New functionality | `feature/add-pagination` |
| `fix/*` | Bug fixes on develop | `fix/soft-delete-not-working` |
| `hotfix/*` | Urgent fix off main | `hotfix/db-connection-timeout` |

---

## Project Structure

```
todo-backend-repo/
├── master/          → port 8080 · database: master_db  (reference/master data)
└── todo/            → port 8081 · database: todos       (todo items)
```

---

## Architecture — Request Flow

```
HTTP Request
     │
     ▼
┌──────────────┐
│  Controller  │  receives HTTP · delegates to service · returns ResponseEntity
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   Service    │  business logic · validation · entity ↔ DTO mapping
└──────┬───────┘
       │
       ▼
┌──────────────┐
│     DAO      │  Spring Data JPA · talks to MySQL
└──────┬───────┘
       │
       ▼
   MySQL DB
```

---

## Concept 1 — Application Entry Point & Configuration

The `@SpringBootApplication` class bootstraps the entire Spring context.  
`application.yaml` configures the datasource, JPA behaviour, and server port.

| Project | Entry Point | Config |
|---------|------------|--------|
| master  | [MasterApplication.java](master/src/main/java/com/master/MasterApplication.java) | [application.yaml](master/src/main/resources/application.yaml) |
| todo    | [TodoApplication.java](todo/src/main/java/com/todo/TodoApplication.java) | [application.yaml](todo/src/main/resources/application.yaml) |

**Key config to understand:**
```yaml
jpa:
  hibernate:
    ddl-auto: update   # auto-creates / alters tables on startup
  show-sql: true       # prints every SQL query to console
```

---

## Concept 2 — Lombok (Boilerplate Elimination)

[Lombok](https://projectlombok.org) is an annotation processor that generates repetitive Java code at compile time — getters, setters, constructors, builders — so you never write them manually.

> **IntelliJ setup required:**  
> `Settings → Plugins → Marketplace → search "Lombok" → Install`  
> `Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing ✓`

### Annotations used in this project

| Annotation | What it generates |
|------------|------------------|
| `@Getter` | `getX()` for every field (or `isX()` for booleans) |
| `@Setter` | `setX(value)` for every field |
| `@NoArgsConstructor` | `new Category()` — required by JPA |
| `@AllArgsConstructor` | Constructor with every field as a parameter — required internally by `@Builder` |
| `@Builder` | A fluent builder: `Category.builder().name("Work").build()` |
| `@Builder.Default` | Preserves a field's default value when using the builder (e.g. `active = true`) |

### Where Lombok is used

| File | Annotations |
|------|-------------|
| [Category.java](master/src/main/java/com/master/entity/Category.java) | `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` |
| [SubCategory.java](master/src/main/java/com/master/entity/SubCategory.java) | `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` |
| [Todo.java](todo/src/main/java/com/todo/entity/Todo.java) | `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` |
| [ApplicationException.java](master/src/main/java/com/master/exception/ApplicationException.java) | `@Getter` — generates `getStatusCode()` |
| [ApplicationException.java](todo/src/main/java/com/todo/exception/ApplicationException.java) | `@Getter` |
| [ApiResponse.java](master/src/main/java/com/master/utility/ApiResponse.java) | `@Getter` — generates `isSuccess()`, `getMessage()`, `getData()` |
| [ApiResponse.java](todo/src/main/java/com/todo/utility/ApiResponse.java) | `@Getter` |

### Builder in practice (service layer)

Before Lombok — positional constructor, fragile if fields reorder:
```java
new Category("Work", "Work related tasks")
```

After Lombok — named, self-documenting, order-independent:
```java
Category.builder()
    .name("Work")
    .description("Work related tasks")
    .build()
```

> **`@Builder.Default` gotcha:** Without this annotation on fields like `active = true` or `status = PENDING`,  
> the Lombok builder ignores the initializer and uses `false` / `null` instead. Always add `@Builder.Default`  
> to any field that has a non-null default value.

---

## Concept 3 — Entity Layer (Database Mapping)  

JPA entities map Java classes to MySQL tables.  
Key annotations: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@ManyToOne`, `@PrePersist`, `@PreUpdate`.

### Master — Reference Data

| Entity | Table | File |
|--------|-------|------|
| Category | `categories` | [Category.java](master/src/main/java/com/master/entity/Category.java) |
| SubCategory | `sub_categories` | [SubCategory.java](master/src/main/java/com/master/entity/SubCategory.java) |

> **Relationship:** `SubCategory` → `@ManyToOne(fetch = LAZY)` → `Category`  
> Many subcategories belong to one category. `LAZY` means Hibernate loads the parent only when accessed.

### Todo — Todo Data

| Entity | Purpose | File |
|--------|---------|------|
| Todo | Main todo item | [Todo.java](todo/src/main/java/com/todo/entity/Todo.java) |
| TodoStatus | Enum: `PENDING` `IN_PROGRESS` `COMPLETED` `CANCELLED` | [TodoStatus.java](todo/src/main/java/com/todo/entity/TodoStatus.java) |
| TodoPriority | Enum: `LOW` `MEDIUM` `HIGH` `URGENT` | [TodoPriority.java](todo/src/main/java/com/todo/entity/TodoPriority.java) |

> **Cross-service design:** `Todo` holds `categoryId` and `subCategoryId` as plain `Long` columns — not `@ManyToOne`.  
> JPA relationships only work within one database. The todo service stores the reference by ID only.

---

## Concept 4 — DAO Layer (Database Queries)

DAOs extend `JpaRepository` to get `save()`, `findById()`, `findAll()`, `deleteById()` for free.  
Custom queries are declared as method names — Spring Data generates the SQL automatically.

| DAO | Entity | File |
|-----|--------|------|
| CategoryDao | Category | [CategoryDao.java](master/src/main/java/com/master/dao/CategoryDao.java) |
| SubCategoryDao | SubCategory | [SubCategoryDao.java](master/src/main/java/com/master/dao/SubCategoryDao.java) |
| TodoDao | Todo | [TodoDao.java](todo/src/main/java/com/todo/dao/TodoDao.java) |

**How derived queries work:**

| Method name | Generated SQL |
|-------------|--------------|
| `findByActiveTrue()` | `WHERE active = true` |
| `findByIdAndActiveTrue(id)` | `WHERE id = ? AND active = true` |
| `existsByNameIgnoreCase(name)` | `WHERE LOWER(name) = LOWER(?)` |
| `findByUserId(userId)` | `WHERE user_id = ?` |
| `findByStatus(status)` | `WHERE status = ?` |

---

## Concept 5 — DTO Layer (Data Transfer Objects)

DTOs are what the API accepts and returns — they are completely separate from entities.  
Implemented as **Java Records** (Java 16+): immutable, no boilerplate, auto-generates constructor + getters.

### Master DTOs

| DTO | Direction | File |
|-----|-----------|------|
| CategoryRequestDto | Client → Server | [CategoryRequestDto.java](master/src/main/java/com/master/dto/CategoryRequestDto.java) |
| CategoryResponseDto | Server → Client | [CategoryResponseDto.java](master/src/main/java/com/master/dto/CategoryResponseDto.java) |
| SubCategoryRequestDto | Client → Server | [SubCategoryRequestDto.java](master/src/main/java/com/master/dto/SubCategoryRequestDto.java) |
| SubCategoryResponseDto | Server → Client | [SubCategoryResponseDto.java](master/src/main/java/com/master/dto/SubCategoryResponseDto.java) |

### Todo DTOs

| DTO | Direction | File |
|-----|-----------|------|
| TodoRequestDto | Client → Server | [TodoRequestDto.java](todo/src/main/java/com/todo/dto/TodoRequestDto.java) |
| TodoResponseDto | Server → Client | [TodoResponseDto.java](todo/src/main/java/com/todo/dto/TodoResponseDto.java) |
| TodoStatusUpdateDto | Client → Server (PATCH only) | [TodoStatusUpdateDto.java](todo/src/main/java/com/todo/dto/TodoStatusUpdateDto.java) |

---

## Concept 6 — Service Layer (Business Logic)

Each service has an **interface** (the contract) and an **implementation** (the logic).  
Controllers depend on the interface — never directly on the implementation.

### Master Services

| Interface | Implementation | File |
|-----------|---------------|------|
| [CategoryService.java](master/src/main/java/com/master/service/CategoryService.java) | [CategoryServiceImpl.java](master/src/main/java/com/master/service/impl/CategoryServiceImpl.java) | master/service |
| [SubCategoryService.java](master/src/main/java/com/master/service/SubCategoryService.java) | [SubCategoryServiceImpl.java](master/src/main/java/com/master/service/impl/SubCategoryServiceImpl.java) | master/service |

### Todo Services

| Interface | Implementation | File |
|-----------|---------------|------|
| [TodoService.java](todo/src/main/java/com/todo/service/TodoService.java) | [TodoServiceImpl.java](todo/src/main/java/com/todo/service/impl/TodoServiceImpl.java) | todo/service |

**What the service is responsible for:**
- Duplicate check before create → throws `409 Conflict`
- `findActive()` helper → throws `404 Not Found` if missing or soft-deleted
- Entity ↔ DTO mapping via `toResponse()`
- Soft delete: sets `active = false`, never deletes the row

---

## Concept 7 — Exception Handling

A single `@RestControllerAdvice` class catches all exceptions across every controller.

| File | Role |
|------|------|
| [ApplicationException.java](master/src/main/java/com/master/exception/ApplicationException.java) | Custom runtime exception carrying an HTTP status code |
| [GlobalExceptionHandler.java](master/src/main/java/com/master/exception/GlobalExceptionHandler.java) | Catches `ApplicationException` → returns clean JSON error |
| [ApplicationException.java](todo/src/main/java/com/todo/exception/ApplicationException.java) | Same pattern for todo service |
| [GlobalExceptionHandler.java](todo/src/main/java/com/todo/exception/GlobalExceptionHandler.java) | Same pattern for todo service |

**Error response shape** (always consistent):
```json
{
  "success": false,
  "message": "Category not found: 99",
  "data": null
}
```

---

## Concept 8 — Utility: ApiResponse Wrapper

Every endpoint — success or error — returns the same JSON envelope.

| File |
|------|
| [ApiResponse.java](master/src/main/java/com/master/utility/ApiResponse.java) |
| [ApiResponse.java](todo/src/main/java/com/todo/utility/ApiResponse.java) |

**Success shape:**
```json
{
  "success": true,
  "message": "Category created successfully",
  "data": { "id": 1, "name": "Work", ... }
}
```

---

## Concept 9 — Controller Layer (REST API)

Controllers receive HTTP requests, call the service, and wrap the result in `ResponseEntity<ApiResponse<T>>`.

### Master Controllers

| Controller | Base Path | File |
|-----------|-----------|------|
| CategoryController | `/api/categories` | [CategoryController.java](master/src/main/java/com/master/controller/CategoryController.java) |
| SubCategoryController | `/api/subcategories` | [SubCategoryController.java](master/src/main/java/com/master/controller/SubCategoryController.java) |

### Todo Controllers

| Controller | Base Path | File |
|-----------|-----------|------|
| TodoController | `/api/todos` | [TodoController.java](todo/src/main/java/com/todo/controller/TodoController.java) |

---

## Concept 10 — Swagger / OpenAPI

`springdoc-openapi` auto-generates interactive API docs from your controllers.  
The `SwaggerConfig` bean sets the title, version, and server URL.

| File | Project |
|------|---------|
| [SwaggerConfig.java](master/src/main/java/com/master/config/SwaggerConfig.java) | master |
| [SwaggerConfig.java](todo/src/main/java/com/todo/config/SwaggerConfig.java) | todo |

| URL | What you see |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | Master API interactive docs |
| http://localhost:8081/swagger-ui.html | Todo API interactive docs |
| http://localhost:8080/v3/api-docs | Master OpenAPI JSON spec |
| http://localhost:8081/v3/api-docs | Todo OpenAPI JSON spec |

---

## API Endpoints — Master (port 8080)

### Category `/api/categories`

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| `POST` | `/api/categories` | Create category | `201` / `409` |
| `GET` | `/api/categories` | Get all active | `200` |
| `GET` | `/api/categories/{id}` | Get by ID | `200` / `404` |
| `PUT` | `/api/categories/{id}` | Update | `200` / `404` |
| `DELETE` | `/api/categories/{id}` | Soft delete | `200` / `404` |

### SubCategory `/api/subcategories`

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| `POST` | `/api/subcategories` | Create subcategory | `201` / `404` / `409` |
| `GET` | `/api/subcategories` | Get all active | `200` |
| `GET` | `/api/subcategories/{id}` | Get by ID | `200` / `404` |
| `GET` | `/api/subcategories/category/{categoryId}` | Get by category | `200` |
| `PUT` | `/api/subcategories/{id}` | Update | `200` / `404` |
| `DELETE` | `/api/subcategories/{id}` | Soft delete | `200` / `404` |

---

## API Endpoints — Todo (port 8081)

### Todo `/api/todos`

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| `POST` | `/api/todos` | Create todo | `201` |
| `GET` | `/api/todos` | Get all todos | `200` |
| `GET` | `/api/todos/{id}` | Get by ID | `200` / `404` |
| `GET` | `/api/todos/user/{userId}` | Get by user | `200` |
| `GET` | `/api/todos/status/{status}` | Get by status | `200` |
| `PUT` | `/api/todos/{id}` | Full update | `200` / `404` |
| `PATCH` | `/api/todos/{id}/status` | Status update only | `200` / `404` |
| `DELETE` | `/api/todos/{id}` | Hard delete | `200` / `404` |

**Status values:** `PENDING` · `IN_PROGRESS` · `COMPLETED` · `CANCELLED`  
**Priority values:** `LOW` · `MEDIUM` · `HIGH` · `URGENT`

---

## Postman Collection

Import the file below into Postman to get all 20 requests pre-configured with collection variables.

→ [Todo-Backend-API.postman_collection.json](Todo-Backend-API.postman_collection.json)

**Collection variables (edit once in Postman):**

| Variable | Default |
|----------|---------|
| `master_base_url` | `http://localhost:8080` |
| `todo_base_url` | `http://localhost:8081` |

---

## How to Run

Two options — Docker Compose (recommended, no local install needed) or running locally with a native MySQL.

---

### Option 1 — Docker Compose (Recommended)

**Prerequisites:** Docker Desktop installed and running. Nothing else.

All three components — MySQL, master service, and todo service — start together with one command.

```bash
# From the repo root: build images and start everything
docker compose up --build

# Run in the background
docker compose up --build -d

# View logs for a specific service
docker compose logs -f master
docker compose logs -f todo

# Stop all containers (DB data is preserved in the volume)
docker compose down

# Stop and wipe the database volume (clean slate)
docker compose down -v
```

**What starts:**

| Container | Role | Host Port |
|-----------|------|-----------|
| `todo_mysql` | MySQL 8.0 — both databases created automatically via `init.sql` | none (internal only) |
| `master_service` | Master Spring Boot service | `8080` |
| `todo_service` | Todo Spring Boot service | `8081` |

> The Spring Boot services will not start until MySQL passes its health check — no manual wait needed.

> **Note:** The MySQL container does not expose a host port. The app services reach it over Docker's internal network — no host port is needed for that. If you also have MySQL installed locally, this avoids a port 3306 conflict.

**Relevant files:**

| File | Purpose |
|------|---------|
| [docker-compose.yml](docker-compose.yml) | Defines all three services, networking, and volume |
| [init.sql](init.sql) | Creates `master_db` and `todos` databases on first MySQL startup |
| [master/Dockerfile](master/Dockerfile) | Multi-stage build for the master service |
| [todo/Dockerfile](todo/Dockerfile) | Multi-stage build for the todo service |

**How the DB connection works in Docker:**

`application.yaml` uses environment variables with a `localhost` fallback:
```yaml
url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/master_db}
```
Docker Compose injects `SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/master_db` where `mysql` is the Docker service name — resolved automatically by Docker's internal DNS. Local development is unaffected.

**Connecting to the Docker MySQL from a GUI tool (optional):**

Because no host port is exposed by default, GUI clients (DBeaver, MySQL Workbench, etc.) cannot reach the Docker MySQL directly. If you need that, add a port mapping that avoids conflicting with any local MySQL:

```yaml
# in docker-compose.yml, under the mysql service
ports:
  - "3307:3306"
```

Then connect your GUI to `localhost:3307`.

---

### Option 2 — Run Locally (Maven)

**Prerequisites:**
- Java 21
- MySQL running locally on port 3306

Create the databases once:
```sql
CREATE DATABASE master_db;
CREATE DATABASE todos;
```

Start each service in a separate terminal:

```bash
# Master service — port 8080
cd master
./mvnw spring-boot:run
```

```bash
# Todo service — port 8081
cd todo
./mvnw spring-boot:run
```

Tables are created automatically by `ddl-auto: update` on first startup.

---

### Recommended API Test Order

1. Create a Category → note the returned `id`
2. Create a SubCategory using that `categoryId`
3. Create a Todo using `categoryId` and `subCategoryId` from above
4. Update todo status via `PATCH /api/todos/{id}/status`

---

## Dependencies (both projects)

| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter-data-jpa` | JPA / Hibernate ORM |
| `spring-boot-starter-webmvc` | REST controllers |
| `mysql-connector-j` | MySQL JDBC driver |
| `lombok` | Compile-time code generation (`@Getter`, `@Setter`, `@Builder` etc.) |
| `springdoc-openapi-starter-webmvc-ui` | Swagger UI + OpenAPI spec generation |

Dependency versions managed by `spring-boot-starter-parent 4.1.0`.

| Project | pom.xml |
|---------|---------|
| master | [pom.xml](master/pom.xml) |
| todo | [pom.xml](todo/pom.xml) |

---

## Unit Tests

### Testing Strategy

Service-layer unit tests only — this is where all the business logic lives and where tests provide the highest value.

| Annotation | What loads | Database needed? |
|-----------|-----------|-----------------|
| `@ExtendWith(MockitoExtension.class)` | Nothing — pure JVM | No |

**Naming convention:** `method_whenCondition_thenOutcome`  
**Style:** BDDMockito (`given` / `then`) throughout for readability.  
**Assertions:** AssertJ (`assertThat`, `assertThatThrownBy`) for fluent, descriptive failures.

Test dependency (both projects, `test` scope):

| Dependency | Provides |
|-----------|---------|
| `spring-boot-starter-data-jpa-test` | JUnit 5, Mockito, AssertJ, Spring Test |

---

### Master Service — Test Files

#### 1. CategoryServiceImplTest
**File:** [master/src/test/java/com/master/service/CategoryServiceImplTest.java](master/src/test/java/com/master/service/CategoryServiceImplTest.java)  
**Approach:** `@ExtendWith(MockitoExtension.class)` · `@Mock CategoryDao` · `@InjectMocks CategoryServiceImpl`

| # | Test Method | Scenario | Expected Behaviour |
|---|------------|---------|-------------------|
| 1 | `create_whenNameIsUnique_returnsSavedCategory` | Name does not exist in DB | Calls `categoryDao.save()` and returns a populated `CategoryResponseDto` |
| 2 | `create_whenNameAlreadyExists_throwsConflict` | `existsByNameIgnoreCase` returns `true` | Throws `ApplicationException` with HTTP `409 Conflict`; `save()` is never called |
| 3 | `getById_whenActiveExists_returnsCategory` | Active category found | Returns DTO with correct `id`, `name`, and `active = true` |
| 4 | `getById_whenNotFound_throwsNotFoundException` | `findByIdAndActiveTrue` returns empty | Throws `ApplicationException` with HTTP `404 Not Found` |
| 5 | `getAll_returnsAllActiveCategories` | Two active categories in DB | Returns list of size 2 with correct names in order |
| 6 | `getAll_whenNoneActive_returnsEmptyList` | No active categories | Returns empty list |
| 7 | `update_whenCategoryExists_returnsUpdatedCategory` | Category found; new name and description provided | Saves updated entity and returns DTO with new values |
| 8 | `update_whenNotFound_throwsNotFoundException` | `findByIdAndActiveTrue` returns empty | Throws `ApplicationException` with HTTP `404 Not Found` |
| 9 | `delete_whenCategoryExists_marksInactiveAndSaves` | Active category found | Sets `active = false` on the entity and calls `save()` (soft delete) |
| 10 | `delete_whenNotFound_throwsNotFoundException` | `findByIdAndActiveTrue` returns empty | Throws `ApplicationException` with HTTP `404 Not Found` |

---

#### 2. SubCategoryServiceImplTest
**File:** [master/src/test/java/com/master/service/SubCategoryServiceImplTest.java](master/src/test/java/com/master/service/SubCategoryServiceImplTest.java)  
**Approach:** `@ExtendWith(MockitoExtension.class)` · `@Mock SubCategoryDao` · `@Mock CategoryDao` · `@InjectMocks SubCategoryServiceImpl`

| # | Test Method | Scenario | Expected Behaviour |
|---|------------|---------|-------------------|
| 1 | `create_whenValidRequest_returnsSavedSubCategory` | Parent category exists; name is unique in that category | Persists subcategory and returns DTO including `categoryId` and `categoryName` |
| 2 | `create_whenParentCategoryNotFound_throwsNotFoundException` | `findByIdAndActiveTrue(categoryId)` returns empty | Throws `ApplicationException` with HTTP `404`; DAO `save()` never called |
| 3 | `create_whenNameExistsInCategory_throwsConflict` | Same name already exists under the same category | Throws `ApplicationException` with HTTP `409 Conflict`; `save()` never called |
| 4 | `getById_whenActiveExists_returnsSubCategory` | Active subcategory found | Returns DTO with correct `id` and parent `categoryId` |
| 5 | `getById_whenNotFound_throwsNotFoundException` | `findByIdAndActiveTrue` returns empty | Throws `ApplicationException` with HTTP `404 Not Found` |
| 6 | `getAll_returnsAllActiveSubCategories` | Two active subcategories in DB | Returns list of size 2 with correct names in order |
| 7 | `getAll_whenNoneActive_returnsEmptyList` | No active subcategories | Returns empty list |
| 8 | `getByCategoryId_returnsSubCategoriesForGivenCategory` | One subcategory belongs to the given category | Returns list of size 1 with matching `categoryId` |
| 9 | `getByCategoryId_whenNone_returnsEmptyList` | No subcategories under the given category | Returns empty list |
| 10 | `update_whenSubCategoryExists_returnsUpdated` | Subcategory and parent category both found | Saves updated entity and returns DTO with new `name` and `description` |
| 11 | `update_whenNotFound_throwsNotFoundException` | `findByIdAndActiveTrue` returns empty | Throws `ApplicationException` with HTTP `404 Not Found` |
| 12 | `delete_whenSubCategoryExists_marksInactiveAndSaves` | Active subcategory found | Sets `active = false` and calls `save()` (soft delete) |
| 13 | `delete_whenNotFound_throwsNotFoundException` | `findByIdAndActiveTrue` returns empty | Throws `ApplicationException` with HTTP `404 Not Found` |

---

### Todo Service — Test Files

#### 5. TodoServiceImplTest
**File:** [todo/src/test/java/com/todo/service/TodoServiceImplTest.java](todo/src/test/java/com/todo/service/TodoServiceImplTest.java)  
**Approach:** `@ExtendWith(MockitoExtension.class)` · `@Mock TodoDao` · `@InjectMocks TodoServiceImpl`

| # | Test Method | Scenario | Expected Behaviour |
|---|------------|---------|-------------------|
| 1 | `create_validRequest_returnsSavedTodoWithDefaults` | No priority specified in request | Persists todo; returned DTO has `status=PENDING` and `priority=MEDIUM` (entity defaults) |
| 2 | `create_withExplicitPriority_returnsSavedTodoWithThatPriority` | `priority=HIGH` in request | Returns DTO with `priority=HIGH` |
| 3 | `getById_whenExists_returnsTodo` | Todo found by ID | Returns DTO with correct `id` and `title` |
| 4 | `getById_whenNotFound_throwsNotFoundException` | `findById` returns empty | Throws `ApplicationException` with HTTP `404 Not Found` |
| 5 | `getAll_returnsAllTodos` | Two todos in DB | Returns list of size 2 with correct titles in order |
| 6 | `getAll_whenNone_returnsEmptyList` | Empty DB | Returns empty list |
| 7 | `getByStatus_returnsTodosFilteredByStatus` | Two PENDING todos in DB | Returns list of size 2; all items have `status=PENDING` |
| 8 | `getByStatus_whenNoneMatch_returnsEmptyList` | No CANCELLED todos | Returns empty list |
| 9 | `update_whenTodoExists_returnsUpdated` | Todo found; new title and priority provided | Saves updated entity; returns DTO with `title="Updated Title"` and `priority=URGENT` |
| 10 | `update_whenNotFound_throwsNotFoundException` | `findById` returns empty | Throws `ApplicationException` with HTTP `404 Not Found` |
| 11 | `updateStatus_whenTodoExists_returnsUpdatedStatus` | Todo found; status changed to `IN_PROGRESS` | Only the `status` field changes; `title` remains the same |
| 12 | `updateStatus_whenNotFound_throwsNotFoundException` | `findById` returns empty | Throws `ApplicationException` with HTTP `404 Not Found` |
| 13 | `delete_whenTodoExists_deletesSuccessfully` | `existsById` returns `true` | Calls `todoDao.deleteById(id)` (hard delete) |
| 14 | `delete_whenNotFound_throwsNotFoundException` | `existsById` returns `false` | Throws `ApplicationException` with HTTP `404 Not Found`; `deleteById` never called |

---

### Test Coverage Summary

| Test File | Project | Test Cases |
|-----------|---------|-----------|
| `CategoryServiceImplTest` | master | 10 |
| `SubCategoryServiceImplTest` | master | 13 |
| `TodoServiceImplTest` | todo | 14 |
| **Total** | | **37** |

### How to Run the Tests

```bash
# Run all tests in the master service
cd master
./mvnw test

# Run all tests in the todo service
cd todo
./mvnw test

# Run a single test class (example)
./mvnw test -Dtest=CategoryServiceImplTest

# Run with verbose output
./mvnw test -Dsurefire.useFile=false
```

> **Note:** No database or running server is required. All tests use Mockito mocks — MySQL is never contacted.
