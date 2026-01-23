# Stage 1
FROM amazoncorretto:25-alpine AS builder
WORKDIR /build

RUN apk add --no-cache bash

COPY . .

RUN chmod +x ./gradlew && ./gradlew clean build -x test --parallel

# Stage 2
FROM amazoncorretto:25-alpine
WORKDIR /app

RUN apk add --no-cache curl

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/build/libs/*[!-plain].jar app.jar

USER spring:spring

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]
