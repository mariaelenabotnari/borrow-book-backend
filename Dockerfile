FROM eclipse-temurin:17-jdk-alpine as build

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom.xml first for better caching
COPY pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S springboot && adduser -S springboot -G springboot

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown springboot:springboot app.jar

# Switch to non-root user
USER springboot

# Expose the port
EXPOSE 10000

# Run the application
CMD java -Dserver.port=${PORT:-10000} -Dserver.address=0.0.0.0 -jar app.jar