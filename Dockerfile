FROM adoptopenjdk/openjdk11:slim

# Install Python and necessary system packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 python3-pip libatlas-base-dev libhdf5-dev \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Upgrade pip and install Python packages
RUN pip3 install --no-cache-dir --upgrade pip && \
    pip3 install --no-cache-dir tensorflow==2.7.0 joblib boto3 pandas
	
# Create application directory
RUN mkdir -p /app
WORKDIR /app

# Copy the JAR file
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar

# Copy Python scripts
COPY src/main/resources/scripts /app/scripts

# Download and unzip New Relic agent
RUN wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip -d /app

# Expose the application port
EXPOSE 5000

# Command to run the application
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-XX:+PrintFlagsFinal", "$JAVA_OPTIONS", "-jar", "/app/app.jar"]
