# Use an OpenJDK base image
FROM openjdk:17-alpine

WORKDIR /app

#RUN apk update && add curl

COPY . /app

# Expose the port your application runs on
EXPOSE 8081

#ADD target/task-management-system.jar task-management-system.jar

CMD ["/bin/bash", "java -jar target/task-management-system.jar"]