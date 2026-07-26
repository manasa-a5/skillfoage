# SkillForge - AI-Driven Adaptive Learning & Exam Generator

Full-stack platform with:
- Frontend: React + Tailwind + Vite
- Backend: Spring Boot (Java 17)
- Database: MySQL
- Auth: JWT (Student / Instructor only)
- AI: OpenAI API for quiz generation

## Project Structure

- `backend/` Spring Boot API
- `frontend/` React web app

## Core Capabilities

- Adaptive learning recommendation engine (Easy / Medium / Hard)
- AI-generated quizzes by topic and difficulty
- Two role-specific dashboards: Student and Instructor
- Analytics and performance tracking

## Backend Setup

1. Ensure MySQL is running.
2. Create/update credentials in `backend/src/main/resources/application.yml` if needed.
3. Set OpenAI key in environment variable:

### Windows PowerShell

```powershell
$env:OPENAI_API_KEY="your_openai_api_key"
```

4. Start backend:

```powershell
cd backend
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`.

## Frontend Setup

```powershell
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`.

## Authentication Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`

Allowed roles:
- `STUDENT`
- `INSTRUCTOR`

## Instructor Endpoints

- `POST /api/instructor/quizzes/generate`
- `GET /api/instructor/quizzes`
- `GET /api/instructor/analytics`

## Student Endpoints

- `GET /api/student/quizzes`
- `GET /api/student/quizzes/{quizId}`
- `POST /api/student/quizzes/{quizId}/submit`
- `GET /api/student/attempts`
- `GET /api/student/analytics`

## Notes

- Update `app.jwt.secret` in `application.yml` to a long secure secret before production.
- The OpenAI call expects JSON quiz response from the model.
