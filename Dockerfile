FROM amazoncorretto:17-alpine

# Set workdir
WORKDIR /app

# Copy the application JAR file
ARG JAR_FILE=target/reciter-2.1.3.jar
COPY ${JAR_FILE} /app/app.jar

# Comment this if you do not have NewRelic integration
RUN wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
	unzip newrelic-java.zip -d /app    

EXPOSE 5000

CMD java -Djava.security.egd=file:/dev/./urandom -XX:+PrintFlagsFinal $JAVA_OPTIONS -jar /app/app.jar
