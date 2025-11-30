FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . /app

RUN ./mvnw -q -DskipTests package

EXPOSE 8080

CMD ["java", "-jar", "target/timetracker-0.0.1-SNAPSHOT.jar"]
