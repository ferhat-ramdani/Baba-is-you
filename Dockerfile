FROM eclipse-temurin:23-jre-jammy

RUN apt-get update && apt-get install -y \
    xvfb \
    x11vnc \
    novnc \
    websockify \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    fonts-dejavu \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY baba.jar /app/baba.jar
COPY lib/ /app/lib/

EXPOSE 6080

ENV DISPLAY=:99

CMD rm -f /tmp/.X99-lock && \
    Xvfb :99 -screen 0 800x600x24 & \
    sleep 2 && \
    x11vnc -display :99 -nopw -forever -shared & \
    websockify --web=/usr/share/novnc/ 6080 localhost:5900 & \
    sleep 2 && \
    java -jar /app/baba.jar