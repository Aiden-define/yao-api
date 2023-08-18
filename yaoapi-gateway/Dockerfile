# Docker 镜像构建
FROM maven:3.5-jdk-8-alpine as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY target ./target

# Run the web service on container startup.
CMD ["java","-jar","/app/target/yaoapi-gateway-0.0.1-SNAPSHOT.jar"]
