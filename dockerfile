# build container
FROM gradle:5.6.2-jdk8 AS BUILD
WORKDIR /src
COPY . /src
RUN gradle build

# running container
FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=BUILD /src/build/libs/Tx-nodes.jar /app/my-application.jar
EXPOSE 80
CMD ["java", "-jar", "my-application.jar"]