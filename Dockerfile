# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

RUN apt-get update -qq && \
    apt-get install -y maven --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

COPY backend/pom.xml .
RUN mvn dependency:go-offline -q

COPY backend/src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Run ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# SerialGC has near-zero native overhead — right choice for a 512MB container.
# ZGC reserves large native memory regions on top of the heap and OOMs on free tier.
# Xms64m: small initial heap so startup doesn't pre-commit memory we don't have.
# Xmx320m: leaves ~190MB for the JVM itself, OS, and Render's agent.
ENV JAVA_OPTS="-XX:+UseSerialGC -Xms64m -Xmx320m -Duser.timezone=Africa/Nairobi"

COPY --from=builder /app/target/kenit-1.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
