FROM openjdk:17-jdk-slim
RUN apt-get update \
 && apt-get install -y netcat-openbsd \
 && rm -rf /var/lib/apt/lists/*
COPY target/carservice-0.0.1-SNAPSHOT.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "carservice-0.0.1-SNAPSHOT.jar"]

