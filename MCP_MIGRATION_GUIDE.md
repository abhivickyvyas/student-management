# MCP Server Migration Guide for Spring Boot Services

**Adding AI-Accessible MCP Endpoints to Existing Spring Boot Applications**

| | |
|---|---|
| **Version** | 1.0 |
| **Date** | February 2026 |
| **Audience** | Backend Engineers, Tech Leads, Architects |
| **Effort per service** | ~1-2 hours |
| **Files changed** | 4 (one new, three modified) |

---

## Table of Contents

1. [What Is MCP and Why Do We Need It?](#1-what-is-mcp-and-why-do-we-need-it)
2. [Architecture Overview](#2-architecture-overview)
3. [Prerequisites Checklist](#3-prerequisites-checklist)
4. [Step-by-Step Migration](#4-step-by-step-migration)
5. [Writing Good Tool Definitions](#5-writing-good-tool-definitions)
6. [Connecting AI Clients](#6-connecting-ai-clients)
7. [Verification Checklist](#7-verification-checklist)
8. [FAQ and Troubleshooting](#8-faq-and-troubleshooting)
9. [Reference: Real Example (Student Management Service)](#9-reference-real-example-student-management-service)

---

## 1. What Is MCP and Why Do We Need It?

**MCP (Model Context Protocol)** is an open standard by Anthropic that lets AI assistants (Claude Desktop, Claude Code, Cursor, etc.) call your backend services directly as "tools" — just like a human would use your REST APIs, but through a structured protocol the AI understands natively.

### Before MCP
```
User -> AI -> "Here's a curl command..." -> User runs curl manually -> Pastes result back
```

### After MCP
```
User -> AI -> [calls your service tool directly] -> Returns result to user
```

### Why This Matters

- **AI agents can operate on your services** — list records, create entries, run reports — by calling MCP tools directly
- **Zero new infrastructure** — MCP runs on the same port as your existing REST API; no sidecars, no proxies, no new deployments
- **Zero changes to existing code** — your services, controllers, repositories, domain models stay untouched
- **Coexistence** — REST APIs and MCP endpoints serve simultaneously from the same application

---

## 2. Architecture Overview

### Before Migration

```
                    +-----------------+
  HTTP clients ---> | :8080           |
  (curl, UI, etc)   | REST Controllers|
                    | Service Layer   |
                    | Repository      |
                    | Database        |
                    +-----------------+
```

### After Migration

```
                    +-----------------+
  HTTP clients ---> | :8080           |
  (curl, UI, etc)   | REST Controllers| ........> /api/v1/...  (unchanged)
                    |                 |
  AI clients -----> | MCP HTTP Layer  | ........> /mcp          (new)
  (Claude, etc)     | MCP Tools       |
                    |                 |
                    | Service Layer   | <--- shared, untouched
                    | Repository      |
                    | Database        |
                    +-----------------+
```

Both REST and MCP call the **same service layer**. The MCP tools class is a thin wrapper — it takes flat parameters from the AI, builds the appropriate request DTO, and delegates to the existing service.

---

## 3. Prerequisites Checklist

Before starting, verify your service meets these requirements:

| Requirement | Minimum Version | How to Check |
|---|---|---|
| **Java** | 17+ | `java -version` |
| **Spring Boot** | 3.5.x+ | Check `build.gradle` or `pom.xml` |
| **Spring dependency-management plugin** | 1.1.7+ | Check `build.gradle` |
| **Gradle** | 8.x+ (if using Gradle) | `./gradlew --version` |
| **Build system** | Gradle or Maven | — |

> **Important:** Spring AI 1.1.x requires Spring Boot 3.5+. If your service is on an older version, you will need to upgrade Spring Boot first. See [Step 1](#step-1-update-build-file) for details.

---

## 4. Step-by-Step Migration

### Step 1: Update Build File

You need to add three things to your build file:
1. Spring AI BOM (Bill of Materials) for version management
2. Spring milestone repository (Spring AI artifacts are published here)
3. The MCP server WebMVC starter dependency

Also upgrade Spring Boot if below 3.5.x.

#### Gradle (`build.gradle`)

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.11'              // Minimum 3.5+, recommended 3.5.11
    id 'io.spring.dependency-management' version '1.1.7'        // Upgrade if below 1.1.7
}

repositories {
    mavenCentral()
    maven { url 'https://repo.spring.io/milestone' }           // ADD — required for Spring AI
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.ai:spring-ai-bom:1.1.0"  // ADD — Spring AI version management
    }
}

dependencies {
    // ... your existing dependencies stay unchanged ...

    implementation 'org.springframework.ai:spring-ai-starter-mcp-server-webmvc'  // ADD
}
```

#### Maven (`pom.xml`)

```xml
<!-- In <properties> -->
<spring-ai.version>1.1.0</spring-ai.version>

<!-- In <dependencyManagement> -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- In <dependencies> -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>

<!-- In <repositories> -->
<repositories>
    <repository>
        <id>spring-milestones</id>
        <url>https://repo.spring.io/milestone</url>
    </repository>
</repositories>
```

> **Verify:** Run `./gradlew compileJava` (or `mvn compile`). It should succeed with no errors.

---

### Step 2: Create the MCP Tools Class

This is the **only new file** you create. It lives in a new `mcp` package alongside your existing code.

#### File location

```
src/main/java/com/yourorg/yourservice/mcp/YourServiceMcpTools.java
```

#### Template

```java
package com.yourorg.yourservice.mcp;

import com.yourorg.yourservice.service.YourService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YourServiceMcpTools {

    private final YourService yourService;

    @Tool(description = "Short, clear description of what this operation does")
    public YourResponseDto your_operation_name(
            @ToolParam(description = "What this parameter is") String requiredParam,
            @ToolParam(description = "Optional detail", required = false) String optionalParam) {

        // Build request DTO from flat params and delegate to service
        YourRequestDto request = YourRequestDto.builder()
                .field1(requiredParam)
                .field2(optionalParam)
                .build();

        return yourService.yourMethod(request);
    }

    // Add more @Tool methods for each operation you want to expose...
}
```

#### Key Rules

| Rule | Why |
|---|---|
| **Use `snake_case` for method names** | AI models work better with snake_case tool names |
| **Use flat parameters, not DTOs** | AI cannot construct complex objects; use primitives (`String`, `Long`, `Integer`) |
| **Pass dates as `String`** | Parse inside the method: `LocalDate.parse(dateStr)` |
| **Write clear `description` on every `@Tool` and `@ToolParam`** | This is what the AI reads to decide when/how to call your tool |
| **Mark optional params with `required = false`** | So the AI knows it doesn't have to provide them |
| **Delegate to the existing service — do not add business logic here** | The MCP tools class is a thin adapter, nothing more |
| **Return your existing response DTOs** | They get serialized to JSON automatically |
| **For void service methods, return a confirmation `String`** | AI needs a response to confirm the action succeeded |

---

### Step 3: Register Tools in Main Application Class

Add a `ToolCallbackProvider` bean that tells the MCP server about your tools class.

```java
import com.yourorg.yourservice.mcp.YourServiceMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class YourApplication {

    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider toolCallbackProvider(YourServiceMcpTools mcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpTools)
                .build();
    }
}
```

> **Multiple tool classes?** Pass them all:
> ```java
> MethodToolCallbackProvider.builder()
>         .toolObjects(orderMcpTools, inventoryMcpTools, customerMcpTools)
>         .build();
> ```

---

### Step 4: Add MCP Server Config to `application.yml`

Add this block under the `spring:` key in your existing `application.yml`:

```yaml
spring:
  ai:
    mcp:
      server:
        name: your-service-name-mcp-server    # Identifies your server to AI clients
        version: 1.0.0                         # Version shown to AI clients
        protocol: streamable                    # Use Streamable HTTP transport
```

That's it. The `spring-ai-starter-mcp-server-webmvc` auto-configures the `/mcp` endpoint.

---

### Summary of Changes

| # | File | Change Type | What to Do |
|---|------|-------------|------------|
| 1 | `build.gradle` / `pom.xml` | Modify | Add Spring AI BOM, milestone repo, MCP starter |
| 2 | `src/.../mcp/YourServiceMcpTools.java` | **New** | Thin wrapper with `@Tool` methods calling your service |
| 3 | `src/.../YourApplication.java` | Modify | Add `ToolCallbackProvider` bean (3 lines) |
| 4 | `application.yml` | Modify | Add 5 lines of MCP server config |

**Zero changes** to your existing service classes, controllers, repositories, domain models, DTOs, or exception handlers.

---

## 5. Writing Good Tool Definitions

The `description` text on `@Tool` and `@ToolParam` is the most important part of this migration. It's what AI models read to understand your service. Poor descriptions = AI won't use your tools correctly.

### Good Descriptions

```java
@Tool(description = "Create a new order for a customer with specified line items and shipping address")
public OrderResponse create_order(
    @ToolParam(description = "The customer's unique ID") Long customerId,
    @ToolParam(description = "Product SKU to order") String productSku,
    @ToolParam(description = "Quantity to order, must be >= 1") Integer quantity,
    @ToolParam(description = "Shipping address as a single line", required = false) String shippingAddress) {
```

### Bad Descriptions

```java
// Too vague — AI doesn't know what "process" means
@Tool(description = "Process order")
public OrderResponse create_order(
    // Missing descriptions — AI has to guess
    @ToolParam(description = "") Long id,
    @ToolParam(description = "sku") String sku,
```

### Description Checklist

- [ ] `@Tool` description says **what the operation does** in plain English
- [ ] Every `@ToolParam` says **what the parameter is**, not just its name
- [ ] For parameters with constraints, mention them (e.g., "must be >= 1", "format: YYYY-MM-DD")
- [ ] Optional parameters are marked `required = false`
- [ ] Method names use `snake_case` and clearly indicate the action (e.g., `get_order_by_id`, `cancel_order`)

---

## 6. Connecting AI Clients

Once your service is running, AI clients connect to it over MCP.

### Claude Code

Create a `.mcp.json` file in your **project root** (the directory where you open Claude Code):

```json
{
  "mcpServers": {
    "your-service-name": {
      "type": "http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

Claude Code picks this up automatically — no restart needed. Just open the project directory and the tools become available.

### Claude Desktop

Add to your Claude Desktop config file:

| OS | Config File Path |
|---|---|
| macOS | `~/Library/Application Support/Claude/claude_desktop_config.json` |
| Windows | `%APPDATA%\Claude\claude_desktop_config.json` |
| Linux | `~/.config/Claude/claude_desktop_config.json` |

```json
{
  "mcpServers": {
    "your-service-name": {
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

Restart Claude Desktop after editing the config.

### VS Code (Copilot)

Press `Ctrl+Shift+P` → "MCP: Add Server" → select **http** → enter `http://localhost:8080/mcp` → choose **Workspace**.

Or create `.vscode/mcp.json`:

```json
{
  "servers": {
    "your-service-name": {
      "type": "http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

### Multiple Services

If you have multiple MCP-enabled services running on different ports, list them all:

```json
{
  "mcpServers": {
    "order-service": {
      "type": "http",
      "url": "http://localhost:8081/mcp"
    },
    "inventory-service": {
      "type": "http",
      "url": "http://localhost:8082/mcp"
    },
    "customer-service": {
      "type": "http",
      "url": "http://localhost:8083/mcp"
    }
  }
}
```

The AI client sees tools from **all** connected servers and can orchestrate across them.

---

## 7. Verification Checklist

After completing the migration, verify with these checks:

### Build

```bash
./gradlew compileJava          # Should succeed with no errors
```

### Start

```bash
./gradlew bootRun              # App starts, no exceptions in logs
```

### REST API Still Works

```bash
curl http://localhost:8080/your/existing/api/endpoint
# Should return the same response as before
```

### MCP Endpoint Responds

```bash
curl -s -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream, application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
# Should return JSON with serverInfo and capabilities
```

### MCP Tools Are Registered

```bash
# After initializing (above), list tools:
curl -s -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}'
```

You should see your tool names in the response.

---

## 8. FAQ and Troubleshooting

### Q: Does MCP replace our REST APIs?

**No.** MCP runs alongside REST on the same port. Your existing REST APIs, Swagger UI, and all HTTP clients continue to work exactly as before. MCP adds an additional `/mcp` endpoint for AI clients.

### Q: Do I need to change my service layer, controllers, or domain models?

**No.** The only new code is the MCP tools wrapper class. It calls your existing service methods. Nothing else changes.

### Q: What Spring Boot version do I need?

**3.5 or higher** (3.5.11 recommended). Spring AI 1.1.x requires Spring Boot 3.5+.

### Q: My service is on an older Spring Boot version. Is upgrading safe?

Spring Boot minor version upgrades (e.g., 3.3 → 3.5) are generally straightforward. Review the [Spring Boot 3.5 release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.5-Release-Notes) for any breaking changes relevant to your service.

### Q: Can I expose only some service methods as MCP tools?

**Yes.** Only methods annotated with `@Tool` in your MCP tools class are exposed. You choose exactly which operations AI clients can access.

### Q: What about authentication/authorization?

The MCP endpoint (`/mcp`) is unauthenticated by default. If your service has Spring Security configured, you may need to permit the MCP endpoints:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/mcp/**").permitAll()   // Allow MCP endpoints
        .anyRequest().authenticated()
    );
    return http.build();
}
```

For production, consider network-level restrictions (only allow AI clients from trusted IPs/VPNs).

### Q: What if my service method throws an exception?

Exceptions propagate naturally. The MCP protocol returns them as error responses to the AI client. The AI will see the error message and can report it to the user or retry.

### Q: Can I have multiple MCP tools classes?

**Yes.** Create separate classes per domain (e.g., `OrderMcpTools`, `InventoryMcpTools`) and pass them all to the builder:

```java
@Bean
public ToolCallbackProvider toolCallbackProvider(
        OrderMcpTools orderTools,
        InventoryMcpTools inventoryTools) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(orderTools, inventoryTools)
            .build();
}
```

### Q: My service uses Maven, not Gradle. Does this work?

**Yes.** See the Maven configuration in [Step 1](#step-1-update-build-file). Everything else (Java code, YAML config) is identical.

### Q: Build fails with "Could not find spring-ai-starter-mcp-server-webmvc"

Make sure you added the Spring milestone repository:

```groovy
// Gradle
maven { url 'https://repo.spring.io/milestone' }
```

```xml
<!-- Maven -->
<repository>
    <id>spring-milestones</id>
    <url>https://repo.spring.io/milestone</url>
</repository>
```

### Q: App starts but `/mcp` returns 404

Check that:
1. `spring-ai-starter-mcp-server-webmvc` is in your dependencies (not just `spring-ai-mcp-server`)
2. You have the `spring.ai.mcp.server` config in `application.yml` with `protocol: streamable`
3. You have a `ToolCallbackProvider` bean registered

---

## 9. Reference: Real Example (Student Management Service)

Here is the complete working example from our Student Management service.

### Files Changed

```
student-management/
  build.gradle                              # MODIFIED — added Spring AI deps
  src/main/resources/application.yml        # MODIFIED — added 4 lines of MCP config
  src/main/java/.../StudentManagementApplication.java  # MODIFIED — added bean
  src/main/java/.../mcp/StudentMcpTools.java           # NEW — tool wrapper
  .mcp.json                                 # NEW — Claude Code client config
```

### build.gradle (key additions)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://repo.spring.io/milestone' }
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.ai:spring-ai-bom:1.1.0"
    }
}

dependencies {
    // ... existing deps unchanged ...
    implementation 'org.springframework.ai:spring-ai-starter-mcp-server-webmvc'
}
```

### application.yml (added block)

```yaml
spring:
  ai:
    mcp:
      server:
        name: student-management-mcp-server
        version: 1.0.0
        protocol: streamable
```

### StudentManagementApplication.java (added bean)

```java
@Bean
public ToolCallbackProvider studentToolCallbackProvider(StudentMcpTools studentMcpTools) {
    return MethodToolCallbackProvider.builder().toolObjects(studentMcpTools).build();
}
```

### StudentMcpTools.java (new file — complete)

```java
package com.demo.studentmanagement.mcp;

import com.demo.studentmanagement.dto.StudentRequest;
import com.demo.studentmanagement.dto.StudentResponse;
import com.demo.studentmanagement.service.StudentService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentMcpTools {

  private final StudentService studentService;

  @Tool(description = "Create a new student record with the given details")
  public StudentResponse create_student(
      @ToolParam(description = "Student's first name") String firstName,
      @ToolParam(description = "Student's last name") String lastName,
      @ToolParam(description = "Student's email address") String email,
      @ToolParam(description = "Student's date of birth in YYYY-MM-DD format", required = false)
          String dateOfBirth,
      @ToolParam(description = "Department the student belongs to", required = false)
          String department,
      @ToolParam(description = "Year the student enrolled", required = false)
          Integer enrollmentYear) {

    StudentRequest request =
        StudentRequest.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .dateOfBirth(dateOfBirth != null ? LocalDate.parse(dateOfBirth) : null)
            .department(department)
            .enrollmentYear(enrollmentYear)
            .build();

    return studentService.createStudent(request);
  }

  @Tool(description = "Get a student by their ID")
  public StudentResponse get_student(
      @ToolParam(description = "The student's unique ID") Long id) {
    return studentService.getStudent(id);
  }

  @Tool(description = "Get a list of all students")
  public List<StudentResponse> get_all_students() {
    return studentService.getAllStudents();
  }

  @Tool(description = "Get all students in a specific department")
  public List<StudentResponse> get_students_by_department(
      @ToolParam(description = "The department name to filter by") String department) {
    return studentService.getStudentsByDepartment(department);
  }

  @Tool(description = "Update an existing student's details. Only provided fields will be updated.")
  public StudentResponse update_student(
      @ToolParam(description = "The student's unique ID") Long id,
      @ToolParam(description = "New first name", required = false) String firstName,
      @ToolParam(description = "New last name", required = false) String lastName,
      @ToolParam(description = "New email address", required = false) String email,
      @ToolParam(description = "New date of birth in YYYY-MM-DD format", required = false)
          String dateOfBirth,
      @ToolParam(description = "New department", required = false) String department,
      @ToolParam(description = "New enrollment year", required = false) Integer enrollmentYear) {

    StudentRequest request =
        StudentRequest.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .dateOfBirth(dateOfBirth != null ? LocalDate.parse(dateOfBirth) : null)
            .department(department)
            .enrollmentYear(enrollmentYear)
            .build();

    return studentService.updateStudent(id, request);
  }

  @Tool(description = "Delete a student by their ID")
  public String delete_student(
      @ToolParam(description = "The student's unique ID") Long id) {
    studentService.deleteStudent(id);
    return "Student with id " + id + " has been deleted successfully.";
  }
}
```

### .mcp.json (Claude Code client config)

```json
{
  "mcpServers": {
    "student-management": {
      "type": "http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

### Result

After these changes, a single `./gradlew bootRun` starts:
- REST API at `http://localhost:8080/api/v1/students` (unchanged)
- MCP server at `http://localhost:8080/mcp` (new)
- Swagger UI at `http://localhost:8080/swagger-ui.html` (unchanged)

All three share the same port, same process, same service layer, same database.
