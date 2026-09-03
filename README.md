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
Configure credentials in `backend/src/main/resources/application.yml`.

### 3. Backend
```bash
cd backend
mvn spring-boot:run
```
Runs on `http://localhost:8080`.

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:3000`.

## Seed Data
On first run, the backend seeds 3-5 team members, a manager account, and several weeks of
sample reports across all statuses (see `SeedDataRunner`). Default credentials are printed to
console on startup.

## Project Structure
```
backend/    Spring Boot API
frontend/   Next.js application
```
