# How I Made My Spring Boot API Talk to AI Agents — And How You Can Try It in 2 Minutes

## The Problem

You have a Spring Boot REST API. It works great for frontends, scripts, and Postman. But now AI assistants like Claude want to interact with your service too — not by reading API docs, but by *calling your service directly as a tool*.

That's what MCP (Model Context Protocol) enables. And adding it to an existing Spring Boot app took me surprisingly little effort.

## What I Built

A Student Management service with two interfaces running simultaneously on the same port:

| Interface | Endpoint | Who Uses It |
|-----------|----------|-------------|
| REST API | `/api/v1/students` | Frontends, curl, Postman |
| MCP Server | `/mcp` | AI assistants (Claude Desktop, Claude Code, VS Code) |

Both hit the **same service layer, same database, same process**. No new infrastructure.

## Try It Yourself (2 Minutes)

You don't need Java or PostgreSQL. Just Docker.

```bash
docker run -p 8080:8080 abhishekvyas19086/student-management:latest
```

Wait for `Started StudentManagementApplication`, then:

**Test the REST API:**
```bash
curl http://localhost:8080/api/v1/students
```
Returns `[]` — empty database, ready to go.

**Open Swagger UI:**
Visit `http://localhost:8080/swagger-ui.html` in your browser to explore all endpoints visually.

**Connect an AI agent:**
Add this to your Claude Desktop config (`~/Library/Application Support/Claude/claude_desktop_config.json` on macOS):

```json
{
  "mcpServers": {
    "student-management": {
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

For Claude Code or VS Code, create a `.mcp.json` file in your working directory:

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

Restart Claude Desktop, then try: *"Create a student named John Doe in the Computer Science department"*

Claude calls the MCP tool, creates the record, and you can verify it with:
```bash
curl http://localhost:8080/api/v1/students
```

Same student, two interfaces. That's the power of MCP.

**Prefer a GUI?** Import the [Postman collection](https://github.com/abhivickyvyas/student-management/blob/master/Student_Management_API.postman_collection.json) included in the repo — all 6 REST endpoints are pre-configured with sample request bodies.

## What It Took to Add MCP

Starting from a standard Spring Boot REST API, here's everything I changed:

| # | What | Change |
|---|------|--------|
| 1 | `build.gradle` | Added Spring AI BOM + 1 dependency |
| 2 | `application.yml` | Added 4 lines of MCP server config |
| 3 | `StudentManagementApplication.java` | Added a `ToolCallbackProvider` bean (3 lines) |
| 4 | `StudentMcpTools.java` | **New file** — thin wrapper around existing service |

**Zero changes** to existing controllers, services, repositories, or domain models.

## The Architecture

![Architecture Diagram](docs/architecture.png)

*[View interactive diagram](https://viewer.diagrams.net/?tags=%7B%7D&lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1&title=Student%20Management%20%E2%80%94%20Dual-Interface%20Architecture&dark=auto#R%3Cmxfile%3E%3Cdiagram%20name%3D%22Architecture%22%20id%3D%22AXeFYUKKPjk3U_7x7Auw%22%3E7Zttc6o4FMc%2FjTPdF3cHCCC%2BtGpvu9POtlf33pl9FyEi20DYELXup78n8qA8tFUu0e1M25kKhxDg9z8nJyfYHhqFL185jpcPzCO0Z2jeSw%2BNe4ahI8uED2nZppb%2BwEkNPg%2B8rNHeMA3%2BI5lRy6yrwCNJqaFgjIogLhtdFkXEFSUb5pxtys0WjJavGmOf1AxTF9O69UfgiWVqdSxtb78lgb%2FMr6xr2ZEQ540zQ7LEHtscmNCkh0acMZFuhS8jQiW8nEt63s0rR4sb4yQSx5zANP9pcvv4IzS8f59u0d%2Ff%2F7GtLw29ZKZEbHMGgrzAseulCCkYdNhcsEhkWulOvp%2BdII9jGvgRbLvQK%2BFgWBMuAqA6zA6EgefJ1tfpxdaYrrKLTcXKk%2FdiaA84ArphutObGD1H6w3AnbTxCtMvd7LnBXYJGIbcXQYC5F9xknUI1yMvBw%2BUMflKWEgE30KT5YFsKNNos5fYyoXMejHy%2FW2%2BnzlZ5mB%2B0fNeA9jIZDhBEuPXJUHHSCIbjBhlfNcj0od9NHGaFLmdzR7BMqIBnJq04mvU%2BepGmS8q4x2owouOwcvZKvKIl6HbSOeaxtLX0HgD41yFfEDpAUgPE2fhgj0RnD2TgyO265D5oqKV0YTcXXHazpGtGmin6sclzroyPzY%2FAuhHlogQR2dibVuKWFsfgfUNhxYk8s4Du5hBdA7b%2Fgiwpxvs%2BzDOG9pfd2cCbqny7v5lMuLE6evasAnu8O7s%2BRAhVe7sqHbnxWJhuI3u7Nlz27KPcecRxTAtlHM%2FkjwLFnfl0nptllembquiPvhQ1EfMazmxPhm5qakaRvKE8P9m%2Fn16Zt6WKhfXjyouf4m3JX8bE%2BXup8LbbEgC1cJUji3QH9xBEPkzuTOWF%2FBwstzdpNaYbGMOrcF2Lat5qEfjmEKnImAws9SuemjoaI72WytFTbOeKwpbLmlFU0eZpEcVp2ee%2B1QlrcnzbTKd7YIqgj4pJRySti1L3lTMGxwH8HcND3KTpKsP7bK61ZDV7YpS%2FUpJoCyt68oL3Raj3btSPYzkUsNUcIJDPKcy96TLD4d6hW67%2FN9CH9RXpo%2Fy%2BriLbJStxj248Ywxehg3Vz1TkzZoD0CWzEvajW8tRDHVBY36Qtoijmc2ieIYc1RLWU11y2siTQlfB267eYPZMG%2BoqICq9Z6pTAX1FfZpKrwVGt9IzJJAMPnc%2B%2BAoZgRjLDB8%2FPE4bJn%2BTxcGacqE6aAS1ysejY6oxAtR6iIs4VY8sN3jrVzt6KYWryx0WJVa3FBWFepHFePJEsdy093SAEKAo%2Ff9f54Gy%2F28MGD32d%2BF0J8rAd3kg0uSyaTVg4boEDb9pqAZ2H2EWw1dMjzmOGk3aDkNqaOyitKvpA51r230oyr692PjvVgoWNdY3sr9qzFznwlvN9jk7ncCUKSsXM8vfDmg8uWAz8n06V6CvWdQJsoRnazPR9dAquhmKIlXe%2F9dx81W3CVvdIZqsshu88BnHKaHPoswneyt1%2BX0%2FXq1lx7J373vM7HA3CfirYA0miXhhEJlvi4%2Fdfd8jQ75mp98a3xRh3ytT741vmaHfO1PvjW%2BVod8ne75FksE7fmii%2FK1O%2BQ7%2BORb49vvkG%2F%2BZuYT8AFgp0vACmZorwDWf7dOQGxeFPGgS8RG94iLdY%2F2PmxdEnC%2BbtINYAWz4A8PuMsqTlcwDX4F8GmDhH1RxF0WcrqCmXCxftHehx01gHvyK3%2F519t3xw7%2BSQBNfgI%3D%3C%2Fdiagram%3E%3C%2Fmxfile%3E#%7B%22pageId%22%3A%22AXeFYUKKPjk3U_7x7Auw%22%7D)*

<details>
<summary>Text version</summary>

```
                    +-----------------+
  HTTP clients ---> | :8080           |
  (curl, UI, etc)   | REST Controllers| --> /api/v1/students
                    |                 |
  AI clients -----> | MCP HTTP Layer  | --> /mcp
  (Claude, etc)     | MCP Tools       |
                    |                 |
                    | Service Layer   | <-- shared, untouched
                    | Repository      |
                    | Database        |
                    +-----------------+
```
</details>

The MCP tools class is just an adapter — it takes flat parameters from the AI, builds request DTOs, and delegates to the same `StudentService` used by REST controllers. No new business logic.

## Step-by-Step: Adding MCP to Your Spring Boot Service

### Step 1: Add the dependency

```groovy
// build.gradle
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
    implementation 'org.springframework.ai:spring-ai-starter-mcp-server-webmvc'
}
```

### Step 2: Create the MCP tools class

This is the only new file. Each `@Tool` method is an operation the AI can call.

```java
@Component
@RequiredArgsConstructor
public class StudentMcpTools {

    private final StudentService studentService;

    @Tool(description = "Create a new student record with the given details")
    public StudentResponse create_student(
            @ToolParam(description = "Student's first name") String firstName,
            @ToolParam(description = "Student's last name") String lastName,
            @ToolParam(description = "Student's email address") String email,
            @ToolParam(description = "Department the student belongs to",
                       required = false) String department,
            @ToolParam(description = "Year the student enrolled",
                       required = false) Integer enrollmentYear) {

        StudentRequest request = StudentRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .department(department)
                .enrollmentYear(enrollmentYear)
                .build();

        return studentService.createStudent(request);
    }

    @Tool(description = "Get a list of all students")
    public List<StudentResponse> get_all_students() {
        return studentService.getAllStudents();
    }

    // ... more tools for get, update, delete
}
```

**Key design choices that matter:**
- **`snake_case` method names** — AI models work better with snake_case tool names
- **Flat parameters, not DTOs** — AI can't construct complex objects; use primitives
- **Clear descriptions on everything** — this is what the AI reads to understand your service. Good descriptions = better AI interactions
- **Dates as `String`** — parse inside the method with `LocalDate.parse(dateStr)`
- **Delegate, don't duplicate** — call your existing service, don't rewrite logic

### Step 3: Register the tools

```java
@SpringBootApplication
public class StudentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentManagementApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider toolCallbackProvider(StudentMcpTools mcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpTools)
                .build();
    }
}
```

### Step 4: Add config

```yaml
spring:
  ai:
    mcp:
      server:
        name: student-management-mcp-server
        version: 1.0.0
        protocol: streamable
```

That's it. Run `./gradlew bootRun` and both REST and MCP are live.

## Dockerizing for Easy Sharing

I wanted anyone to try this without installing Java or a database. So I Dockerized the app with:

- **Multi-stage Dockerfile** — builds from source, no pre-built JAR needed
- **H2 in-memory database** for the Docker profile — no external database
- **Spring profiles** — `docker` profile uses H2, default profile uses PostgreSQL for local dev

The Dockerfile:

```dockerfile
# Build
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon || true
COPY src src
RUN ./gradlew bootJar --no-daemon

# Run
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
```

One command, everything works:

```bash
docker run -p 8080:8080 abhishekvyas19086/student-management:latest
```

## What Clicked for Me

1. **MCP is not a replacement for REST** — it's a parallel interface for AI agents. Your existing APIs stay untouched.

2. **Descriptions are your new API documentation** — the `@Tool` and `@ToolParam` descriptions are what AI reads to understand your service. Invest time in writing them well.

3. **The AI-friendly tool API is different from REST** — flat parameters instead of DTOs, snake_case names, Strings instead of complex types. Think of it as designing an interface for a very capable but literal-minded consumer.

4. **Zero infrastructure overhead** — MCP runs on the same port as your REST API. No sidecars, no proxies, no new deployments. One process serves both.

## What's Next

As AI agents become mainstream, our backend services need to be accessible not just to UIs and scripts, but also to AI assistants. MCP makes that possible with minimal changes to your existing codebase.

I've also created:
- A detailed [MCP Migration Guide](https://github.com/abhivickyvyas/student-management/blob/master/MCP_MIGRATION_GUIDE.md) with templates, FAQ, troubleshooting, and more
- A [Postman collection](https://github.com/abhivickyvyas/student-management/blob/master/Student_Management_API.postman_collection.json) for quickly testing all REST endpoints
- An [architecture diagram](https://github.com/abhivickyvyas/student-management/blob/master/docs/architecture.png) ([interactive view](https://viewer.diagrams.net/?tags=%7B%7D&lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1&title=Student%20Management%20%E2%80%94%20Dual-Interface%20Architecture&dark=auto#R%3Cmxfile%3E%3Cdiagram%20name%3D%22Architecture%22%20id%3D%22AXeFYUKKPjk3U_7x7Auw%22%3E7Zttc6o4FMc%2FjTPdF3cHCCC%2BtGpvu9POtlf33pl9FyEi20DYELXup78n8qA8tFUu0e1M25kKhxDg9z8nJyfYHhqFL185jpcPzCO0Z2jeSw%2BNe4ahI8uED2nZppb%2BwEkNPg%2B8rNHeMA3%2BI5lRy6yrwCNJqaFgjIogLhtdFkXEFSUb5pxtys0WjJavGmOf1AxTF9O69UfgiWVqdSxtb78lgb%2FMr6xr2ZEQ540zQ7LEHtscmNCkh0acMZFuhS8jQiW8nEt63s0rR4sb4yQSx5zANP9pcvv4IzS8f59u0d%2Ff%2F7GtLw29ZKZEbHMGgrzAseulCCkYdNhcsEhkWulOvp%2BdII9jGvgRbLvQK%2BFgWBMuAqA6zA6EgefJ1tfpxdaYrrKLTcXKk%2FdiaA84ArphutObGD1H6w3AnbTxCtMvd7LnBXYJGIbcXQYC5F9xknUI1yMvBw%2BUMflKWEgE30KT5YFsKNNos5fYyoXMejHy%2FW2%2BnzlZ5mB%2B0fNeA9jIZDhBEuPXJUHHSCIbjBhlfNcj0od9NHGaFLmdzR7BMqIBnJq04mvU%2BepGmS8q4x2owouOwcvZKvKIl6HbSOeaxtLX0HgD41yFfEDpAUgPE2fhgj0RnD2TgyO265D5oqKV0YTcXXHazpGtGmin6sclzroyPzY%2FAuhHlogQR2dibVuKWFsfgfUNhxYk8s4Du5hBdA7b%2Fgiwpxvs%2BzDOG9pfd2cCbqny7v5lMuLE6evasAnu8O7s%2BRAhVe7sqHbnxWJhuI3u7Nlz27KPcecRxTAtlHM%2FkjwLFnfl0nptllembquiPvhQ1EfMazmxPhm5qakaRvKE8P9m%2Fn16Zt6WKhfXjyouf4m3JX8bE%2BXup8LbbEgC1cJUji3QH9xBEPkzuTOWF%2FBwstzdpNaYbGMOrcF2Lat5qEfjmEKnImAws9SuemjoaI72WytFTbOeKwpbLmlFU0eZpEcVp2ee%2B1QlrcnzbTKd7YIqgj4pJRySti1L3lTMGxwH8HcND3KTpKsP7bK61ZDV7YpS%2FUpJoCyt68oL3Raj3btSPYzkUsNUcIJDPKcy96TLD4d6hW67%2FN9CH9RXpo%2Fy%2BriLbJStxj248Ywxehg3Vz1TkzZoD0CWzEvajW8tRDHVBY36Qtoijmc2ieIYc1RLWU11y2siTQlfB267eYPZMG%2BoqICq9Z6pTAX1FfZpKrwVGt9IzJJAMPnc%2B%2BAoZgRjLDB8%2FPE4bJn%2BTxcGacqE6aAS1ysejY6oxAtR6iIs4VY8sN3jrVzt6KYWryx0WJVa3FBWFepHFePJEsdy093SAEKAo%2Ff9f54Gy%2F28MGD32d%2BF0J8rAd3kg0uSyaTVg4boEDb9pqAZ2H2EWw1dMjzmOGk3aDkNqaOyitKvpA51r230oyr692PjvVgoWNdY3sr9qzFznwlvN9jk7ncCUKSsXM8vfDmg8uWAz8n06V6CvWdQJsoRnazPR9dAquhmKIlXe%2F9dx81W3CVvdIZqsshu88BnHKaHPoswneyt1%2BX0%2FXq1lx7J373vM7HA3CfirYA0miXhhEJlvi4%2Fdfd8jQ75mp98a3xRh3ytT741vmaHfO1PvjW%2BVod8ne75FksE7fmii%2FK1O%2BQ7%2BORb49vvkG%2F%2BZuYT8AFgp0vACmZorwDWf7dOQGxeFPGgS8RG94iLdY%2F2PmxdEnC%2BbtINYAWz4A8PuMsqTlcwDX4F8GmDhH1RxF0WcrqCmXCxftHehx01gHvyK3%2F519t3xw7%2BSQBNfgI%3D%3C%2Fdiagram%3E%3C%2Fmxfile%3E#%7B%22pageId%22%3A%22AXeFYUKKPjk3U_7x7Auw%22%7D)) showing the dual-interface design

Check them out if you're looking to add MCP to your own Spring Boot services.

---

*Tech stack: Java 17, Spring Boot 3.5, Spring AI 1.1, MCP Streamable HTTP, H2/PostgreSQL, Docker*

*Source code and migration guide: [GitHub](https://github.com/abhivickyvyas/student-management)*
