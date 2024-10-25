# Use a Debian base image
FROM debian:bullseye

# Install necessary packages including OpenJDK and Python
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-11-jre python3 python3-pip libatlas-base-dev libhdf5-dev && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Upgrade pip and install Python packages
RUN pip3 install --no-cache-dir --upgrade pip && \
    pip3 install --no-cache-dir tensorflow==2.7.0 joblib boto3 pandas

# Create application directory
RUN mkdir -p /app
WORKDIR /app

# Copy the JAR file (update ARG as needed)
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar

# Copy Python scripts
COPY src/main/resources/scripts /app/scripts

# Set JAVA_OPTIONS environment variable to avoid errors
ENV JAVA_OPTIONS="-Xmx1024m"

# Expose the application port
EXPOSE 5000

# Command to run the application
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-XX:+PrintFlagsFinal", "-jar", "/app/app.jar"]
