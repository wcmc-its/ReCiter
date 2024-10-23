# Stage 1: Build Python dependencies
FROM python:3.9-slim AS python-env

# Install necessary build tools and libraries
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libffi-dev \
    libblas-dev \
    gfortran \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Upgrade PIP to the latest version
RUN pip install --upgrade pip setuptools

# Create application directory
WORKDIR /app

# Copy Python scripts
COPY src/main/resources/scripts /app/scripts

# Copy the requirements.txt file
COPY src/main/resources/scripts/requirements.txt /app/requirements.txt

# Install Python packages
RUN pip install --no-cache-dir -r /app/requirements.txt

# Install TensorFlow
RUN pip install --no-cache-dir tensorflow==2.12.0  # Adjust the version as needed

# Stage 2: Build Java application
FROM adoptopenjdk/openjdk11:alpine-jre AS java-env

# Install Python and necessary dependencies
RUN apk add --no-cache python3 py3-pip

# Create application directory
RUN mkdir -p /app
WORKDIR /app

# Copy the JAR file
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar

# Copy Python dependencies from the first stage
COPY --from=python-env /app /app

# Comment this if you do not have NewRelic integration
RUN wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip -d /app 

# Expose the desired port
EXPOSE 5000

# Command to run the application
CMD java -Djava.security.egd=file:/dev/./urandom -XX:+PrintFlagsFinal $JAVA_OPTIONS -jar /app/app.jar
