# Stage 1: Build OpenJDK11
FROM ubuntu:20.04 AS build-openjdk

# Set non-interactive mode
ENV DEBIAN_FRONTEND=noninteractive
# Install dependencies for building OpenJDK and update CA certificates
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    wget \
	unzip \
	ca-certificates \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Download and extract OpenJDK source
RUN wget https://download.java.net/java/GA/jdk11/openjdk-11_linux-x64_bin.tar.gz && \
    tar -xzf openjdk-11_linux-x64_bin.tar.gz && \
    mv jdk-11* /opt/openjdk
	
# Stage 2: Final image
FROM ubuntu:20.04
	
# Set non-interactive mode
ENV DEBIAN_FRONTEND=noninteractive						  

# Install Python 3.12 and other required packages
RUN apt-get update && apt-get install -y \
    software-properties-common \
    && add-apt-repository ppa:deadsnakes/ppa \
    && apt-get update && apt-get install -y \
    python3.12 \
    wget \
    unzip \
	libatlas-base-dev \
    libhdf5-dev \
    libhdf5-serial-dev \
    libjpeg-dev \
    zlib1g-dev \				   
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*												 

# Set python3.12 as the default python3
RUN update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.12 1

# Install pip using get-pip.py
RUN wget https://bootstrap.pypa.io/get-pip.py && \
    python3 get-pip.py && \
    rm get-pip.py				 
# Set the working directory
WORKDIR /app						   

# Copy the requirements file
COPY src/main/resources/scripts/requirements.txt .

# Install the required Python packages
RUN python3 -m pip install --no-cache-dir -r requirements.txt

# Create the application directory
RUN mkdir -p /app
WORKDIR /app


# Copy the JAR file
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar

# Copy OpenJDK to the final image
COPY --from=build-openjdk /opt/openjdk /opt/openjdk

# Set environment variables for Java
ENV JAVA_HOME=/opt/openjdk
ENV PATH="$JAVA_HOME/bin:$PATH"

# Copy Python scripts
COPY src/main/resources/scripts /app/scripts

# Give execute permissions to the scripts folder and its contents
RUN chmod -R +x /app/scripts

# Comment this if you do not have NewRelic integration
RUN wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip -d /app && \
	rm newrelic-java.zip					

EXPOSE 5000

# Command to run the application
CMD ["java", "-Xms1024m", "-Xmx2g", "-jar", "/app/app.jar"]
