# syntax=docker/dockerfile:1.2
# Build stage
FROM arm64v8/maven:3-openjdk-18-slim AS build

ENV MAVEN_OPTS='-Xmx2048m'

WORKDIR /app
COPY . .
COPY .m2 /root/.m2
RUN mvn -f pom.xml clean package

# Package stage
FROM arm64v8/openjdk:18-ea-18-jdk-slim
WORKDIR /app
COPY --from=build /app/target/DiscordBot.jar .
COPY --from=build /app/keys.jks .
CMD ["java","-jar","./DiscordBot.jar"]