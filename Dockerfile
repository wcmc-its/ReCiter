FROM adoptopenjdk/openjdk11:alpine-jre

# Install Python
RUN apk add --no-cache python3 py3-pip

#copy the requirements.txt file 
COPY src/main/resources/scripts/requirements.txt .

# Install any needed packages specified in requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Create application directory
RUN mkdir -p /app
WORKDIR /app

# Copy the JAR file
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar

# Copy Python scripts
COPY src/main/resources/scripts /app/scripts

# Comment this if you do not have NewRelic integration
RUN wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip -d /app    

#Expose the desired port
EXPOSE 5000

# Command to run the application
CMD java -Djava.security.egd=file:/dev/./urandom -XX:+PrintFlagsFinal $JAVA_OPTIONS -jar /app/app.jar
