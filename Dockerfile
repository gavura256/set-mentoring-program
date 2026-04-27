# ---- Stage 1: build ----
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

# Copy Maven wrapper and POM first — cached until pom.xml changes
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -Dmaven.test.skip=true -q

# Extract layered jar — largest layers first for best cache reuse
RUN java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted

# ---- Stage 2: runtime ----
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S bookshop && adduser -S bookshop -G bookshop

# Copy layers in stable-to-volatile order — only the last layer changes per build
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER bookshop

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# exec replaces sh with the JVM as PID 1 so SIGTERM reaches the JVM for graceful shutdown
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
