# BorrowBook Backend

A Spring Boot backend application for a physical book borrowing system where users can share and borrow books from each other.

## What does it let us do... so far?

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

### Prerequisites
- Docker & Docker Compose
- Java 17+ (if running locally)

### Using Docker
```bash
# Start PostgreSQL and Redis services
docker-compose up -d

# Set required environment variables
export SPRING_MAIL_USERNAME=your-email@domain.com
export SPRING_MAIL_PASSWORD=your-email-password
export GOOGLE_BOOK_API_KEY=your-google-books-api-key
export CLIENT_ID=your-google-oauth-client-id
export CLIENT_SECRET=your-google-oauth-client-secret

# JWT & Security
export JWT_SECRET=your-jwt-secret-key
export JWT_EXPIRATION=86400000
export REFRESH_TOKEN_EXPIRATION=604800000

# Database
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/borrowbook
export SPRING_DATASOURCE_USERNAME=your-db-username
export SPRING_DATASOURCE_PASSWORD=your-db-password

# Redis
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379
export SPRING_REDIS_PASSWORD=your-redis-password

# Email configuration
export SPRING_MAIL_HOST=smtp.purelymail.com
export SPRING_MAIL_PORT=587
export FRONTEND_URL=http://localhost:3000

# Run the application
./mvnw spring-boot:run
```
