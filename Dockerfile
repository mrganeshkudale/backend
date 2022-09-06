FROM alpine/git
WORKDIR /app
RUN git clone https://github.com/mrganeshkudale/backend.git

FROM maven:3.5-jdk-8-alpine
WORKDIR /app
COPY --from=0 /app/backend /app
RUN mvn install

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=1 /app/target/backend-1.0.0.jar /app
CMD ["java -jar backend-1.0.0.jar"]
