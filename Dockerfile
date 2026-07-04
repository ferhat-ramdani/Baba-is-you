FROM eclipse-temurin:23

RUN apt-get update && apt-get install -y \
    xvfb \
    x11vnc \
    fluxbox \
    wget \
    tar \
    python3 \
    python3-numpy \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    fonts-dejavu \
    && rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/novnc/noVNC/archive/refs/tags/v1.4.0.tar.gz && \
    tar -xzf v1.4.0.tar.gz && \
    mv noVNC-1.4.0 /novnc && \
    wget https://github.com/novnc/websockify/archive/refs/tags/v0.11.0.tar.gz && \
    tar -xzf v0.11.0.tar.gz && \
    mv websockify-0.11.0 /novnc/utils/websockify && \
    rm *.tar.gz

WORKDIR /app

COPY baba.jar /app/baba.jar
COPY lib/ /app/lib/
COPY src/ /app/src/

EXPOSE 6080

ENV DISPLAY=:99

CMD rm -f /tmp/.X99-lock && \
    Xvfb :99 -screen 0 800x600x24 & \
    sleep 2 && \
    fluxbox -display :99 & \
    x11vnc -display :99 -nopw -forever -shared & \
    /novnc/utils/novnc_proxy --vnc localhost:5900 --listen 6080 & \
    sleep 2 && \
    java -jar /app/baba.jar