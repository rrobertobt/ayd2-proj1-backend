# the base image
FROM maven:latest

# Copy project files into the Docker image
COPY . /app

# Also copy application.prod.yaml to the resources directory
COPY src/main/resources/application.prod.yaml /app/src/main/resources/application.yaml

# Set the working directory
WORKDIR /app

# Generate JAR file using Maven and copy it to the root of the image
RUN mvn clean package -DskipTests && \
    cp $(find target -maxdepth 1 -name "*.jar" ! -name "*sources*" ! -name "*javadoc*") application.jar


# Set the default command to run the Java application
ENTRYPOINT ["java", "-Xmx2048M", "-jar", "/app/application.jar"]