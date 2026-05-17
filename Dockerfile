FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /src

COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties* ./

RUN chmod +x ./gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew buildFatJar --no-daemon

FROM gcr.io/distroless/java17-debian13:nonroot AS runtime

WORKDIR /app

COPY --from=build /src/build/libs/*-all.jar /app/app.jar

EXPOSE 8080

CMD ["/app/app.jar"]