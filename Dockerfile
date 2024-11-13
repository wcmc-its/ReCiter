FROM adoptopenjdk/openjdk11:alpine-jre

RUN mkdir -p /app
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar
# Comment this if you do not have NewRelic integration
RUN wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip -d /app    
EXPOSE 5000
CMD java -Djava.security.egd=file:/dev/./urandom -XX:+PrintFlagsFinal $JAVA_OPTIONS -jar /app/app.jar
