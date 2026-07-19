# Monthly Planner

A responsive monthly cost planner converted from the original ChatGPT Site into a complete Spring Boot application.

## Features

- Monthly navigation with a separate plan for every month
- Editable income
- Add, edit, and delete expense categories
- Live totals, remaining balance, percentages, progress bars, and donut chart
- Spring Boot REST API with validation
- H2 file database persistence
- Automatic localStorage fallback for the GitHub Pages version
- Docker and Render deployment configuration
- GitHub Actions for CI and GitHub Pages publishing

## Run locally

Requirements: Java 21 and Maven 3.6.3 or newer.

```bash
mvn spring-boot:run
```

Open `http://localhost:8080`.

## Test

```bash
mvn clean verify
```

## REST API

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/planner/{year}/{month}` | Load or create a monthly plan |
| PUT | `/api/planner/{year}/{month}/income` | Update income |
| POST | `/api/planner/{year}/{month}/expenses` | Add an expense |
| PUT | `/api/planner/{year}/{month}/expenses/{id}` | Update an expense |
| DELETE | `/api/planner/{year}/{month}/expenses/{id}` | Delete an expense |

## Deployment

- **GitHub Pages:** publishes the interactive frontend automatically. It uses browser localStorage because Pages cannot run Java.
- **Spring Boot backend:** deploy the repository as a Docker service on Render using `render.yaml`, or run the Docker image on any Java/Docker host.

## Technology

Spring Boot 3.5.16, Java 21, Spring Web, Spring Data JPA, Bean Validation, H2, HTML, CSS, and vanilla JavaScript.
