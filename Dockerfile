# Stage 1: Build Python environment using Debian
FROM python:3.8-slim AS python-env

# Install necessary system packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    libatlas-base-dev libhdf5-dev && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Upgrade pip and install Python packages
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir tensorflow==2.7.0 joblib boto3 pandas

# Stage 2: Build Java environment using Alpine
FROM adoptopenjdk/openjdk11:alpine AS java-env

# Install wget and unzip
RUN apk add --no-cache wget unzip

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build context
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar

# Copy Python packages from the previous stage
COPY --from=python-env /usr/local/lib/python3.8/site-packages /usr/local/lib/python3.8/site-packages

# Copy Python binaries to ensure pip and python commands work
COPY --from=python-env /usr/local/bin/python3 /usr/local/bin/
COPY --from=python-env /usr/local/bin/pip /usr/local/bin/

# Download and unzip New Relic agent
RUN wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip -d /app

# Expose the application port
EXPOSE 5000

# Command to run the application
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-XX:+PrintFlagsFinal", "-jar", "/app/app.jar"]
