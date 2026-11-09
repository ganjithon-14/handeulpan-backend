# ---- 1단계: 빌드 (JDK 21) ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# 의존성 레이어를 먼저 받아 두면 소스만 바뀔 때 빌드가 빨라집니다.
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src src
RUN ./gradlew --no-daemon bootJar -x test \
    && find build/libs -name '*.jar' ! -name '*-plain.jar' -exec cp {} /workspace/app.jar \;

# ---- 2단계: 실행 (JRE 21) ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --no-create-home app
COPY --from=build /workspace/app.jar app.jar
USER app

EXPOSE 8080
# 메모리가 작은 서버(t3.small)에서 DB·Caddy와 같이 돌리므로 힙을 제한합니다. (compose의 JAVA_OPTS로 바꿀 수 있음)
ENV JAVA_OPTS="-Xms128m -Xmx512m"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
