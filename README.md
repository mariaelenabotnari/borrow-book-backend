# BorrowBook Backend

A Spring Boot backend application for a physical book borrowing system where users can share and borrow books from each other.

## What does it do?

BorrowBook allows users to:
- Register/login with email verification or Google OAuth
- Add books to their collection using Google Books API
- Search and browse available books from other users
- Send and manage borrow requests
- Track borrowed books and due dates

## Architecture

- **Framework**: Spring Boot 3.5.5 with Java 17
- **Database**: PostgreSQL for persistent data
- **Cache**: Redis (dual setup - cache + persistence for refresh tokens)
- **Authentication**: JWT tokens, Google Auth
- **Security**: CSRF protection, rate limiting, email verification, etc
- **External APIs**: Google Books API for book metadata
- **Email**: Thymeleaf templates for verification emails sent via Purelymail

### Key Components
- JWT-based authentication with cookie storage
- Rate limiting for login/registration attempts
- Two-factor authentication via email codes
- RESTful API with OpenAPI documentation
- Docker containerization ready

## How to Run

### Option 1: Full Docker Setup (No Java Required)

```bash
# 1. Copy the example environment file
cp .env.example .env

# 2. Edit .env with your own secrets (mail, OAuth, API keys)

# 3. Build and run everything
docker-compose -f docker-compose.full.yml up --build
```

The app will be available at `http://localhost:8080`.

### Option 2: Local Development (Java Required)

**Prerequisites:**
- Docker & Docker Compose
- Java 17+

```bash
# 1. Start databases only
docker-compose up -d

# 2. Copy and configure environment
cp .env.example .env
# Edit .env with your secrets

# 3. Run the Spring Boot app
./mvnw spring-boot:run
```

### Required Environment Variables

See `.env.example` for all required variables:
- `SPRING_MAIL_USERNAME` / `SPRING_MAIL_PASSWORD` - Email service credentials
- `CLIENT_ID` / `CLIENT_SECRET` - Google OAuth credentials
- `GOOGLE_BOOK_API_KEY` - Google Books API key
- `MAILGUN_API_KEY` / `MAILGUN_DOMAIN` - Mailgun credentials (for production)
- `MAIL_PROVIDER` - Email provider (`smtp` for local, `mailgun` for production)

## CI/CD Pipeline

The project uses GitHub Actions for continuous integration and deployment.

### Pipeline Stages

1. **Build** - Compiles the project with Maven
2. **Test** - Runs all unit tests
3. **Deploy** - Triggers deployment on Render (only on push/merge to `master`)

### How It Works

- On **pull request** to `master`: Build and test only (validates the code)
- On **push/merge** to `master`: Build → Test → Deploy to production

### Secrets Management

| Secret | Location | Purpose |
|--------|----------|---------|
| `RENDER_DEPLOY_HOOK` | GitHub Secrets | Webhook URL to trigger Render deployment |
| All other env vars | Render Dashboard | App configuration (DB, Redis, OAuth, API keys, etc.) |

This separation keeps deployment credentials in GitHub while application secrets stay in Render's environment.

