FROM clojure:openjdk-17-lein as builder

WORKDIR /app
COPY project.clj ./
RUN lein deps

COPY . ./
RUN lein uberjar

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/agent-api-0.1.0-SNAPSHOT-standalone.jar ./app.jar

EXPOSE 3000

CMD ["java", "-jar", "app.jar"]