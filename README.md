# 🛡️ Business Continuity Planner — Tool 37

**Internship Capstone Project | 6-Member Team | Sprint: April 14 – May 9, 2026**

---

## What Is This Project?

Imagine a big company — like a bank or a hospital. What happens if their computers crash? What if there's a fire, a flood, or a cyberattack? Do they know what to do?

That's what a **Business Continuity Plan (BCP)** is — it's a document that says:
> *"If X goes wrong, here is exactly what we do to recover."*

**This tool is a web application that helps companies create, manage, and track all their Business Continuity Plans in one place.**

Instead of keeping BCPs in random Word documents or Excel sheets, this app gives you:
- A clean dashboard to see the status of all your plans at a glance
- The ability to create, edit, and delete plans easily
- A readiness score (0–100) for each plan so you know which ones need attention
- AI-powered suggestions to improve your plans
- Charts and analytics to understand your overall preparedness
- A full audit trail — every change is automatically recorded

---

## My Role — Java Developer 2

This was a 6-person team project. I was **Java Developer 2**, responsible for building the database layer, the complete React frontend, and several backend features.

### What I built:

**1. Database Schema (Flyway Migrations)**
- Designed and wrote the SQL to create all database tables
- `V1__init.sql` — creates the `continuity_plans` table with all columns and indexes
- `V2__audit_log.sql` — creates the `audit_log` table to record every change
- These migrations run automatically when the app starts — no manual database setup needed

**2. Repository Layer (Spring Data JPA)**
- Wrote `ContinuityPlanRepository.java` — the interface that talks to the database
- Added search queries, stats queries, soft-delete filtering, and pagination support
- Wrote `AuditLogRepository.java` — for storing and retrieving audit records

**3. Service & Controller Enhancements**
- Built `ContinuityPlanService.java` — all the business logic (create, update, soft-delete, search, stats)
- Built `ContinuityPlanController.java` — all the REST API endpoints the frontend calls
- Added `GET /api/plans/search` — search by keyword, status, and department
- Added `GET /api/plans/stats` — returns counts and average score for the dashboard
- Added `GET /api/plans/export` — downloads all plans as a CSV file
- Added `POST /api/plans/upload` — accepts file attachments (validates size and type)
- Added proper HTTP status codes (201 for create, 204 for delete, 404 when not found)

**4. Audit Logging (Spring AOP)**
- Built `AuditAspect.java` — uses Spring AOP so every create, update, and delete is automatically logged to the `audit_log` table without writing any extra code in the service

**5. Error Handling**
- Built `GlobalExceptionHandler.java` — every API error returns consistent JSON with status, message, and timestamp
- Built `ResourceNotFoundException.java` — returns a proper 404 when a plan doesn't exist

**6. Data Seeder**
- Built `DataLoader.java` — automatically inserts 30 realistic Business Continuity Plans across 7 departments when the app starts for the first time, so the demo always looks great

**7. Complete React Frontend (from scratch)**
- **Login page** — with username/password form and error handling
- **Auth system** — `AuthContext.jsx` stores the login token, `ProtectedRoute.jsx` blocks access to pages if not logged in
- **Dashboard** — 8 KPI cards showing counts and averages, plus 3 live charts built with Recharts (bar chart for status, horizontal bar for departments, pie chart for priority)
- **Plans page** — full create/edit form with all fields, 500ms debounced search, status and department dropdowns, paginated table with Edit/Delete/View buttons, Export CSV button, and an AI panel that shows AI responses
- **Plan Detail page** — shows all plan fields, a colour-coded readiness score badge (green/yellow/red), RTO and RPO display, and AI recommendations panel
- **Analytics page** — status pie chart, department bar chart, and a score trend line chart
- **Responsive design** — works on mobile (375px), tablet (768px), and desktop (1280px)

**8. Integration Tests**
- Wrote `ContinuityPlanControllerTest.java` — 12 MockMvc tests covering all endpoints, validation errors, 404 handling, soft-delete, CSV export, and auth

---

## Team Structure

| Role | Responsibilities |
|---|---|
| **Java Developer 1** | Spring Boot setup, JWT authentication, Redis caching, email notifications, Docker Compose |
| **Java Developer 2 (me)** | Database schema, Repository layer, full React frontend, Audit logging, Data seeder, Tests |
| **AI Developer 1** | Flask setup, `/describe` and `/recommend` AI endpoints, prompt templates |
| **AI Developer 2** | Groq API client, `/generate-report` endpoint, security review, prompt tuning |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite, Recharts, Axios |
| Backend | Java 17, Spring Boot 3, Spring AOP |
| Database | PostgreSQL 15 (Docker) / H2 in-memory (local dev) |
| Migrations | Flyway |
| AI Service | Python 3, Flask, Groq API (LLaMA-3.3-70b) |
| Containers | Docker + Docker Compose |
| API Docs | Swagger UI (SpringDoc OpenAPI) |

---

## How to Run (Local — No Docker Needed)

The easiest way to run the app for development or testing.

### What you need to install

Only **one thing** — Java JDK 17 or higher.
Download from: **https://adoptium.net**

> Maven is NOT needed — the project includes `mvnw` which downloads Maven automatically.

---

### Step 1 — Start the Backend

Open a terminal, go into the `backend` folder, and run:

```bash
cd backend
./mvnw spring-boot:run
```

On Windows you can also use:
```cmd
mvnw.cmd spring-boot:run
```

Wait about 10 seconds. You will see:

```
Successfully applied 2 migrations      ← database tables created
Started Tool37Application              ← backend is ready
```

The backend is now running at **http://localhost:8080**

- Swagger UI (all API docs): http://localhost:8080/swagger-ui.html
- H2 database browser: http://localhost:8080/h2-console

---

### Step 2 — Start the Frontend

Open a **second terminal**, go into the `frontend` folder, and run:

```bash
cd frontend
npm install
npm run dev
```

> If `npm install` gives errors, run: `npm install --legacy-peer-deps`

The frontend is now running at **http://localhost:5173**

Open that URL in your browser. Login with:
- **Username:** `admin`
- **Password:** `admin123`

---

### Step 3 — AI Service (Optional)

The AI buttons ("AI Describe", "Get Recommendations") need the Python service running. If you skip this, those buttons show an error but everything else works fine.

```bash
# From the project root folder
pip install -r requirements.txt
python app.py
```

---

## How to Run (With Docker — Recommended for Demo Day)

This starts all 5 services (backend, frontend, database, Redis, AI) with one command.

### Step 1 — Create the .env file

```bash
cp .env.example .env
```

Open `.env` and add your Groq API key (free at https://console.groq.com):
```
GROQ_API_KEY=gsk_your_key_here
```

### Step 2 — Start everything

```bash
docker-compose up --build
```

### Step 3 — Open the app

| What | URL |
|---|---|
| App (frontend) | http://localhost |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| AI Service | http://localhost:5000/health |

Login: `admin` / `admin123`

### To stop

```bash
docker-compose down
```

To wipe data and start fresh:
```bash
docker-compose down -v && docker-compose up --build
```

---

## API Endpoints

Full interactive docs at: **http://localhost:8080/swagger-ui.html**

| Method | URL | What it does |
|---|---|---|
| GET | /api/plans | List all plans (paginated) |
| GET | /api/plans/{id} | Get one plan (404 if not found) |
| POST | /api/plans | Create a new plan |
| PUT | /api/plans/{id} | Update a plan |
| DELETE | /api/plans/{id} | Soft-delete a plan |
| GET | /api/plans/search | Search by keyword, status, department |
| GET | /api/plans/stats | Dashboard stats (counts, avg score) |
| GET | /api/plans/export | Download all plans as CSV |
| POST | /api/plans/upload | Upload a file attachment |
| POST | /api/auth/login | Login — returns token |
| GET | /api/audit-log | Full audit trail |

---

## Running Tests

```bash
cd backend
./mvnw test
```

12 integration tests covering all major endpoints.

---

## Common Problems

**Port 8080 already in use**
→ A previous backend run is still alive. On Windows run: `netstat -ano | findstr :8080` then `taskkill /PID <number> /F`

**Frontend blank page**
→ Make sure the backend is running first on port 8080.

**"AI service unavailable"**
→ The Python service is not running. Run `python app.py` from the project root.

**`npm install` fails**
→ Run `npm install --legacy-peer-deps`

**Flyway migration error**
→ Run `docker-compose down -v && docker-compose up --build` to reset the database.
