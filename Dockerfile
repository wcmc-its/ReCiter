FROM openjdk:11-jre-slim

RUN mkdir -p /app
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar
EXPOSE 5000
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-XX:+PrintFlagsFinal","$JAVA_OPTIONS","-jar","/app/app.jar"]