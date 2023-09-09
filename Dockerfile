FROM gradle:8.2-jdk17-alpine AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle build --no-daemon -x test


# Run: docker build . -t lag-analyzer --build-arg JAR_VERSION=${egrep -oh '[0-9]{1,}\.[0-9]{1,}\.[0-9]{1,}-SNAPSHOT{,1}' build.gradle }
FROM eclipse-temurin:17-jre-alpine

ARG JAR_VERSION
RUN echo $JAR_VERSION

RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*${JAR_VERSION}.jar /app/spring-boot-application.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/spring-boot-application.jar"]

