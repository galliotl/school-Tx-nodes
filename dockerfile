FROM openjdk:8-jre-alpine
ARG JAR_FILE
RUN mkdir /app
COPY $[JAR_FILE] /app/my-application.jar
WORKDIR /app
EXPOSE 7777 8777
CMD ["java", "-jar", "my-application.jar"]