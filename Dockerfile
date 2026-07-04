FROM eclipse-temurin:23-jre-alpine

RUN apk add --no-cache xvfb x11vnc novnc bash

WORKDIR /app

COPY baba.jar /app/baba.jar
COPY lib/ /app/lib/

EXPOSE 6080

ENV DISPLAY=:99

CMD rm -f /tmp/.X99-lock && \
    Xvfb :99 -screen 0 800x600x16 & \
    sleep 2 && \
    x11vnc -display :99 -nopw -forever -shared & \
    /usr/bin/novnc_proxy --vnc localhost:5900 --listen 6080 & \
    sleep 2 && \
    java -jar /app/baba.jar