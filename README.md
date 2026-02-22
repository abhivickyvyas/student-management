# Student Management

A Spring Boot application that manages student records via REST APIs **and** an MCP (Model Context Protocol) server — both running simultaneously on port 8080.

## Quick Start with Docker

No Java or PostgreSQL install needed — just Docker. The Docker image uses an embedded H2 database, so everything runs in a single container.

### Option 1: Pull from Docker Hub (no source code needed)

```bash
docker run -p 8080:8080 abhishekvyas19086/student-management:latest
```

### Option 2: Build from source

```bash
docker compose up --build
```

Once you see `Started StudentManagementApplication`, everything is ready:

| Endpoint | URL |
|----------|-----|
| REST API | `http://localhost:8080/api/v1/students` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| MCP (Streamable HTTP) | `http://localhost:8080/mcp` |

> **Note:** Data is stored in-memory (H2) and resets on container restart. For persistent data, use the [local dev setup with PostgreSQL](#local-development-with-postgresql).

---

## Try the REST API

### Option A: Import the Postman Collection (recommended)

A ready-made Postman collection with all endpoints is included in the repo:

1. Open Postman → **Import** → select `Student_Management_API.postman_collection.json`
2. All 6 endpoints are pre-configured with sample request bodies
3. Hit **Send** on any request — no manual URL or header setup needed

### Option B: Use Swagger UI

Visit `http://localhost:8080/swagger-ui.html` in your browser to explore and test all endpoints visually.

### Option C: curl

**Create a student:**
```bash
curl -X POST http://localhost:8080/api/v1/students \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","department":"Computer Science","enrollmentYear":2023}'
```

**List all students:**
```bash
curl http://localhost:8080/api/v1/students
```

**Get by department:**
```bash
curl "http://localhost:8080/api/v1/students?department=Computer%20Science"
```

**Update / Delete:**
```bash
curl -X PUT http://localhost:8080/api/v1/students/1 \
  -H "Content-Type: application/json" \
  -d '{"department":"Mathematics"}'

curl -X DELETE http://localhost:8080/api/v1/students/1
```

---

## Try the MCP Server

The MCP server exposes the same student operations as tools that AI clients can call directly.

### Available MCP Tools

| Tool | Description |
|------|-------------|
| `create_student` | Create a new student record |
| `get_student` | Get a student by ID |
| `get_all_students` | List all students |
| `get_students_by_department` | Filter students by department |
| `update_student` | Update an existing student |
| `delete_student` | Delete a student by ID |

### Step 1: Start the server

```bash
docker run -p 8080:8080 abhishekvyas19086/student-management:latest
```

### Step 2: Connect your AI client

**Claude Code / VS Code** — add `.mcp.json` to your project root:

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

**Claude Desktop** — add to your config file (`~/Library/Application Support/Claude/claude_desktop_config.json` on macOS, `%APPDATA%\Claude\claude_desktop_config.json` on Windows):

```json
{
  "mcpServers": {
    "student-management": {
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

**VS Code (Copilot)** — press `Ctrl+Shift+P` → "MCP: Add Server" → select **http** → enter `http://localhost:8080/mcp` → choose **Workspace**.

Or create `.vscode/mcp.json`:

```json
{
  "servers": {
    "student-management": {
      "type": "http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

Restart Claude Desktop after updating the config. Claude Code and VS Code pick up `.mcp.json` automatically.

### Step 3: Try it

Ask your AI client: *"Create a student named John Doe in the Computer Science department"*

The AI calls the MCP tool, creates the record, and you can verify:
```bash
curl http://localhost:8080/api/v1/students
```

---

## Architecture

![Architecture Diagram](docs/architecture.png)

> *Open `docs/architecture.drawio` in [draw.io](https://app.diagrams.net/) to edit. Export to PNG after changes.*

Both REST and MCP interfaces share the same service layer, repository, and database. The MCP tools class (`StudentMcpTools`) is a thin wrapper that takes flat parameters from the AI, builds request DTOs, and delegates to the same `StudentService` used by REST controllers.

### Database Profiles

| Profile | Database | Persistence | Use Case |
|---------|----------|-------------|----------|
| `docker` (default in container) | H2 (in-memory) | Resets on restart | Quick demo, Docker |
| `default` (local dev) | PostgreSQL | Persistent | Local development |

---

## Local Development with PostgreSQL

### Prerequisites

- Java 17+
- PostgreSQL running on `localhost:5432`
- A database named `student_management_db`

```bash
# Create the database (if it doesn't exist)
psql -U postgres -c "CREATE DATABASE student_management_db;"
```

### Running

```bash
./gradlew bootRun
```

This starts **both** the REST API and the MCP server on port 8080 — no separate processes needed.

### How It Works

The `spring-ai-starter-mcp-server-webmvc` starter adds MCP Streamable HTTP transport on top of the existing Spring MVC app. Both the REST controllers and MCP tool endpoints are served by the same embedded Tomcat on port 8080. REST requests go through `@RestController` mappings while MCP clients connect via the `/mcp` endpoint.

---

## Related Resources

- **[MCP Migration Guide](MCP_MIGRATION_GUIDE.md)** — step-by-step guide to add MCP to any Spring Boot service
- **[Postman Collection](Student_Management_API.postman_collection.json)** — import into Postman for all 6 REST endpoints
- **[LinkedIn Article](linkedin-article.md)** — full write-up on the architecture and implementation
- **[Architecture Diagram](docs/architecture.drawio)** — editable draw.io diagram of the dual-interface architecture
