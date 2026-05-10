# ── Stage 1: Build ──────────────────────────────────────────────────────────
# jammy = Ubuntu 22.04 = glibc. Alpine uses musl — ZGC crashes on musl at startup.
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

RUN apt-get update -qq && \
    apt-get install -y maven --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

# Pom first — deps get cached as a separate layer, only rebuilt if pom changes
COPY backend/pom.xml .
RUN mvn dependency:go-offline -q

COPY backend/src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Run ────────────────────────────────────────────────────────────
# jre-jammy has glibc → ZGC works. Render free tier = 512MB RAM total.
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational -Xms128m -Xmx384m -Duser.timezone=Africa/Nairobi"

COPY --from=builder /app/target/kenit-1.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
