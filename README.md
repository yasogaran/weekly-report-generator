# Weekly Report Generator & Team Dashboard

A full-stack internal tool for weekly team reporting with a manager review/correction workflow
and a team analytics dashboard.

## Stack
- **Backend**: Spring Boot, Spring Security (JWT), Spring Data JPA, MySQL
- **Frontend**: Next.js (App Router), Tailwind CSS, Recharts

## Setup Instructions

### 1. Install dependencies
- Java 17+, Maven, Node.js 18+, MySQL 8+

### 2. Database
```bash
mysql -u root -p -e "CREATE DATABASE weekly_reports;"
```

### 3. Local secrets
Copy `application-local.yml.example` to `application-local.yml` and fill in your MySQL
password and a JWT secret:
```bash
cp backend/src/main/resources/application-local.yml.example backend/src/main/resources/application-local.yml
```
Edit `backend/src/main/resources/application-local.yml` with your real MySQL password and a
long random JWT secret (at least 32 characters). This file is gitignored — it never gets
committed. `application.yml` has no real secrets in it and is safe to commit.

### 4. Backend
```bash
cd backend
mvn spring-boot:run
```
Runs on `http://localhost:8080`.

### 5. Frontend
```bash
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:3000`.

## Seed Data
On first run, the backend automatically seeds a manager, 4 team members, 3 projects, and a
handful of reports across mixed statuses (see `DataSeeder`, which runs via `CommandLineRunner`
on every startup but only inserts data when the `users` table is empty — it never overwrites
real data). No manual command is needed:

```bash
cd backend
mvn spring-boot:run
```

If you want to re-seed from scratch (e.g. after messing up local data), drop and recreate the
database, then start the backend again:
```bash
mysql -u root -p -e "DROP DATABASE weekly_reports; CREATE DATABASE weekly_reports;"
cd backend
mvn spring-boot:run
```

## Project Structure
```
backend/    Spring Boot API
frontend/   Next.js application
```

## MySQL Setup
1. Install MySQL 8+ and make sure the server is running locally on port `3306`.
2. Create the database (also auto-created on backend startup via
   `createDatabaseIfNotExist=true`, so this step is optional):
   ```bash
   mysql -u root -p -e "CREATE DATABASE weekly_reports;"
   ```
3. Set your MySQL username/password for the backend in
   `backend/src/main/resources/application-local.yml` (copied from the `.example` file in
   step 3 of Setup Instructions above):
   ```yaml
   spring:
     datasource:
       username: root            # omit this line to use the "root" default
       password: your-mysql-password-here
   ```
   Alternatively, set `DB_USERNAME` / `DB_PASSWORD` environment variables instead of using
   `application-local.yml` — both are read by `application.yml`.

## Sample User Credentials (local dev only)
These are the accounts currently used for local testing (matches `docs/api/postman.json`).
**Never reuse these in a real deployment.**

| Role         | Email                | Password      |
|--------------|-----------------------|---------------|
| Manager      | manager@test.com      | password123   |
| Team Member  | member1@test.com      | password123   |
| Team Member  | member2@test.com      | password123   |

> **Note:** `DataSeeder.java` currently seeds different placeholder emails
> (`manager@example.com`, `alice@example.com`, etc.) than the ones above. If you're running
> against a freshly seeded database, use the `DataSeeder.java` emails instead — the ones in
> this table only exist if you've registered them manually via `POST /api/auth/register`
> (or the seeder is updated to match `postman.json`).
