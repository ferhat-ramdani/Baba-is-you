FROM eclipse-temurin:23-jdk AS builder

RUN apt-get update && apt-get install -y ant

WORKDIR /app

COPY . .

RUN ant compile

FROM eclipse-temurin:23-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
    tigervnc-standalone-server \
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

RUN wget -q https://github.com/novnc/noVNC/archive/refs/tags/v1.4.0.tar.gz && \
    tar -xzf v1.4.0.tar.gz && \
    mv noVNC-1.4.0 /novnc && \
    ln -s /novnc/vnc.html /novnc/index.html && \
    wget -q https://github.com/novnc/websockify/archive/refs/tags/v0.11.0.tar.gz && \
    tar -xzf v0.11.0.tar.gz && \
    mv websockify-0.11.0 /novnc/utils/websockify && \
    rm *.tar.gz

WORKDIR /app

COPY --from=builder /app/classes /app/classes
COPY --from=builder /app/lib /app/lib
COPY --from=builder /app/src /app/src

EXPOSE 6080

ENV DISPLAY=:99

CMD rm -rf /tmp/.X11-unix && \
    mkdir -p /tmp/.X11-unix && \
    chmod 1777 /tmp/.X11-unix && \
    Xvnc :99 -SecurityTypes None -geometry 800x600 -depth 16 & \
    sleep 2 && \
    /novnc/utils/novnc_proxy --vnc localhost:5900 --listen ${PORT:-6080} & \
    sleep 2 && \
    java -Xmx256m -cp "classes:lib/*" baba.engine.Main