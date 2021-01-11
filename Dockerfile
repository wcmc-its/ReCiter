FROM adoptopenjdk/openjdk11:alpine-jre

RUN mkdir -p /app
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar
EXPOSE 5000
CMD java -Djava.security.egd=file:/dev/./urandom $JAVA_OPTIONS -jar /app/app.jar