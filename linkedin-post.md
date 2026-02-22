I made my Spring Boot API talk to AI agents. You can try it in 2 minutes.

One command. No Java. No database. Just Docker:

docker run -p 8080:8080 abhishekvyas19086/student-management:latest

Now ask Claude: "Create a student named John Doe in Computer Science"

Claude calls your service directly, creates the record, and you can verify it with curl. Same data, same service layer — two interfaces (REST + AI).

The wild part? Adding MCP to an existing Spring Boot app took:
- 1 dependency
- 1 new class
- 4 lines of YAML
- 0 changes to existing code

I wrote a full article breaking down the architecture, the code, and a step-by-step guide to add MCP to any Spring Boot service. Repo includes a Postman collection so you can test all REST endpoints instantly. Link in comments.

#SpringBoot #MCP #AI #Claude #SpringAI #Java #Docker #BackendDevelopment
