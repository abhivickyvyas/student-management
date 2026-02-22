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

![Architecture Diagram](docs/Dual-Interface%20Architecture.png)

> *[View interactive diagram](https://viewer.diagrams.net/?tags=%7B%7D&lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1&title=Student%20Management%20%E2%80%94%20Dual-Interface%20Architecture&dark=auto#R%3Cmxfile%3E%3Cdiagram%20name%3D%22Architecture%22%20id%3D%22AXeFYUKKPjk3U_7x7Auw%22%3E7Zttc6o4FMc%2FjTPdF3cHCCC%2BtGpvu9POtlf33pl9FyEi20DYELXup78n8qA8tFUu0e1M25kKhxDg9z8nJyfYHhqFL185jpcPzCO0Z2jeSw%2BNe4ahI8uED2nZppb%2BwEkNPg%2B8rNHeMA3%2BI5lRy6yrwCNJqaFgjIogLhtdFkXEFSUb5pxtys0WjJavGmOf1AxTF9O69UfgiWVqdSxtb78lgb%2FMr6xr2ZEQ540zQ7LEHtscmNCkh0acMZFuhS8jQiW8nEt63s0rR4sb4yQSx5zANP9pcvv4IzS8f59u0d%2Ff%2F7GtLw29ZKZEbHMGgrzAseulCCkYdNhcsEhkWulOvp%2BdII9jGvgRbLvQK%2BFgWBMuAqA6zA6EgefJ1tfpxdaYrrKLTcXKk%2FdiaA84ArphutObGD1H6w3AnbTxCtMvd7LnBXYJGIbcXQYC5F9xknUI1yMvBw%2BUMflKWEgE30KT5YFsKNNos5fYyoXMejHy%2FW2%2BnzlZ5mB%2B0fNeA9jIZDhBEuPXJUHHSCIbjBhlfNcj0od9NHGaFLmdzR7BMqIBnJq04mvU%2BepGmS8q4x2owouOwcvZKvKIl6HbSOeaxtLX0HgD41yFfEDpAUgPE2fhgj0RnD2TgyO265D5oqKV0YTcXXHazpGtGmin6sclzroyPzY%2FAuhHlogQR2dibVuKWFsfgfUNhxYk8s4Du5hBdA7b%2Fgiwpxvs%2BzDOG9pfd2cCbqny7v5lMuLE6evasAnu8O7s%2BRAhVe7sqHbnxWJhuI3u7Nlz27KPcecRxTAtlHM%2FkjwLFnfl0nptllembquiPvhQ1EfMazmxPhm5qakaRvKE8P9m%2Fn16Zt6WKhfXjyouf4m3JX8bE%2BXup8LbbEgC1cJUji3QH9xBEPkzuTOWF%2FBwstzdpNaYbGMOrcF2Lat5qEfjmEKnImAws9SuemjoaI72WytFTbOeKwpbLmlFU0eZpEcVp2ee%2B1QlrcnzbTKd7YIqgj4pJRySti1L3lTMGxwH8HcND3KTpKsP7bK61ZDV7YpS%2FUpJoCyt68oL3Raj3btSPYzkUsNUcIJDPKcy96TLD4d6hW67%2FN9CH9RXpo%2Fy%2BriLbJStxj248Ywxehg3Vz1TkzZoD0CWzEvajW8tRDHVBY36Qtoijmc2ieIYc1RLWU11y2siTQlfB267eYPZMG%2BoqICq9Z6pTAX1FfZpKrwVGt9IzJJAMPnc%2B%2BAoZgRjLDB8%2FPE4bJn%2BTxcGacqE6aAS1ysejY6oxAtR6iIs4VY8sN3jrVzt6KYWryx0WJVa3FBWFepHFePJEsdy093SAEKAo%2Ff9f54Gy%2F28MGD32d%2BF0J8rAd3kg0uSyaTVg4boEDb9pqAZ2H2EWw1dMjzmOGk3aDkNqaOyitKvpA51r230oyr692PjvVgoWNdY3sr9qzFznwlvN9jk7ncCUKSsXM8vfDmg8uWAz8n06V6CvWdQJsoRnazPR9dAquhmKIlXe%2F9dx81W3CVvdIZqsshu88BnHKaHPoswneyt1%2BX0%2FXq1lx7J373vM7HA3CfirYA0miXhhEJlvi4%2Fdfd8jQ75mp98a3xRh3ytT741vmaHfO1PvjW%2BVod8ne75FksE7fmii%2FK1O%2BQ7%2BORb49vvkG%2F%2BZuYT8AFgp0vACmZorwDWf7dOQGxeFPGgS8RG94iLdY%2F2PmxdEnC%2BbtINYAWz4A8PuMsqTlcwDX4F8GmDhH1RxF0WcrqCmXCxftHehx01gHvyK3%2F519t3xw7%2BSQBNfgI%3D%3C%2Fdiagram%3E%3C%2Fmxfile%3E#%7B%22pageId%22%3A%22AXeFYUKKPjk3U_7x7Auw%22%7D) | Edit: open `docs/architecture.drawio` in [draw.io](https://app.diagrams.net/)*

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
- **[Architecture Diagram](docs/Dual-Interface%20Architecture.png)** ([interactive view](https://viewer.diagrams.net/?tags=%7B%7D&lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1&title=Student%20Management%20%E2%80%94%20Dual-Interface%20Architecture&dark=auto#R%3Cmxfile%3E%3Cdiagram%20name%3D%22Architecture%22%20id%3D%22AXeFYUKKPjk3U_7x7Auw%22%3E7Zttc6o4FMc%2FjTPdF3cHCCC%2BtGpvu9POtlf33pl9FyEi20DYELXup78n8qA8tFUu0e1M25kKhxDg9z8nJyfYHhqFL185jpcPzCO0Z2jeSw%2BNe4ahI8uED2nZppb%2BwEkNPg%2B8rNHeMA3%2BI5lRy6yrwCNJqaFgjIogLhtdFkXEFSUb5pxtys0WjJavGmOf1AxTF9O69UfgiWVqdSxtb78lgb%2FMr6xr2ZEQ540zQ7LEHtscmNCkh0acMZFuhS8jQiW8nEt63s0rR4sb4yQSx5zANP9pcvv4IzS8f59u0d%2Ff%2F7GtLw29ZKZEbHMGgrzAseulCCkYdNhcsEhkWulOvp%2BdII9jGvgRbLvQK%2BFgWBMuAqA6zA6EgefJ1tfpxdaYrrKLTcXKk%2FdiaA84ArphutObGD1H6w3AnbTxCtMvd7LnBXYJGIbcXQYC5F9xknUI1yMvBw%2BUMflKWEgE30KT5YFsKNNos5fYyoXMejHy%2FW2%2BnzlZ5mB%2B0fNeA9jIZDhBEuPXJUHHSCIbjBhlfNcj0od9NHGaFLmdzR7BMqIBnJq04mvU%2BepGmS8q4x2owouOwcvZKvKIl6HbSOeaxtLX0HgD41yFfEDpAUgPE2fhgj0RnD2TgyO265D5oqKV0YTcXXHazpGtGmin6sclzroyPzY%2FAuhHlogQR2dibVuKWFsfgfUNhxYk8s4Du5hBdA7b%2Fgiwpxvs%2BzDOG9pfd2cCbqny7v5lMuLE6evasAnu8O7s%2BRAhVe7sqHbnxWJhuI3u7Nlz27KPcecRxTAtlHM%2FkjwLFnfl0nptllembquiPvhQ1EfMazmxPhm5qakaRvKE8P9m%2Fn16Zt6WKhfXjyouf4m3JX8bE%2BXup8LbbEgC1cJUji3QH9xBEPkzuTOWF%2FBwstzdpNaYbGMOrcF2Lat5qEfjmEKnImAws9SuemjoaI72WytFTbOeKwpbLmlFU0eZpEcVp2ee%2B1QlrcnzbTKd7YIqgj4pJRySti1L3lTMGxwH8HcND3KTpKsP7bK61ZDV7YpS%2FUpJoCyt68oL3Raj3btSPYzkUsNUcIJDPKcy96TLD4d6hW67%2FN9CH9RXpo%2Fy%2BriLbJStxj248Ywxehg3Vz1TkzZoD0CWzEvajW8tRDHVBY36Qtoijmc2ieIYc1RLWU11y2siTQlfB267eYPZMG%2BoqICq9Z6pTAX1FfZpKrwVGt9IzJJAMPnc%2B%2BAoZgRjLDB8%2FPE4bJn%2BTxcGacqE6aAS1ysejY6oxAtR6iIs4VY8sN3jrVzt6KYWryx0WJVa3FBWFepHFePJEsdy093SAEKAo%2Ff9f54Gy%2F28MGD32d%2BF0J8rAd3kg0uSyaTVg4boEDb9pqAZ2H2EWw1dMjzmOGk3aDkNqaOyitKvpA51r230oyr692PjvVgoWNdY3sr9qzFznwlvN9jk7ncCUKSsXM8vfDmg8uWAz8n06V6CvWdQJsoRnazPR9dAquhmKIlXe%2F9dx81W3CVvdIZqsshu88BnHKaHPoswneyt1%2BX0%2FXq1lx7J373vM7HA3CfirYA0miXhhEJlvi4%2Fdfd8jQ75mp98a3xRh3ytT741vmaHfO1PvjW%2BVod8ne75FksE7fmii%2FK1O%2BQ7%2BORb49vvkG%2F%2BZuYT8AFgp0vACmZorwDWf7dOQGxeFPGgS8RG94iLdY%2F2PmxdEnC%2BbtINYAWz4A8PuMsqTlcwDX4F8GmDhH1RxF0WcrqCmXCxftHehx01gHvyK3%2F519t3xw7%2BSQBNfgI%3D%3C%2Fdiagram%3E%3C%2Fmxfile%3E#%7B%22pageId%22%3A%22AXeFYUKKPjk3U_7x7Auw%22%7D)) — dual-interface architecture diagram
