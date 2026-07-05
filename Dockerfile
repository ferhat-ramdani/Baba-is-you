# Build stage
FROM eclipse-temurin:23-jdk AS builder

RUN apt-get update && apt-get install -y --no-install-recommends ant && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .
RUN ant clean jar

# Runtime stage — tiny JRE only, no X11, no Webswing
FROM eclipse-temurin:23-jre

WORKDIR /app

# Copy built jar, source assets (images + levels), and lib (zen jar)
COPY --from=builder /app/baba.jar /app/baba.jar
COPY --from=builder /app/lib /app/lib
COPY --from=builder /app/src /app/src

EXPOSE 8080

CMD java -Djava.awt.headless=true -jar baba.jar --web ${PORT:-8080}