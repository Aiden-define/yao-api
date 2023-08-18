# Docker 镜像构建
FROM maven:3.5-jdk-8-alpine as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","/app/target/yaoapi-interface-0.0.1-SNAPSHOT.jar"]


# 先打包，直接上传
## Docker 镜像构建
#FROM maven:3.5-jdk-8-alpine as builder
#
## Copy local code to the container image.
#WORKDIR /app
#COPY pom.xml .
#COPY target ./target
#
## Build a release artifact.
## RUN mvn package -DskipTests
#
## Run the web service on container startup.
#CMD ["java","-jar","/app/target/logistics-backend-0.0.1-SNAPSHOT.jar"]
