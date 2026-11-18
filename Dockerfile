# 멀티스테이지 빌드. linux/amd64(t3)와 linux/arm64(t4g·Oracle Ampere) 모두에서 빌드된다.
# 나중에 ARM으로 옮길 때 이 파일을 그대로 쓸 수 있도록 베이스 이미지를 멀티아키로 골랐다.

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /src

# 의존성 레이어를 먼저 만든다. src만 바뀌면 이 레이어는 캐시로 재사용된다.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
# Windows에서 커밋된 gradlew는 CRLF일 수 있고, 그러면 리눅스에서 실행되지 않는다.
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# t3.small(2GB)에서 빌드해도 OOM이 나지 않도록 Gradle 힙을 묶어둔다.
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx512m -Dorg.gradle.daemon=false"
RUN ./gradlew --no-daemon dependencies --quiet || true

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test


FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 루트로 돌리지 않는다.
RUN useradd -r -u 1001 -m app
COPY --from=build /src/build/libs/triplinker.jar app.jar
RUN chown app:app /app/app.jar
USER app

ENV TZ=Asia/Seoul
EXPOSE 8081

# 설정은 이미지에 굽지 않고 /config 볼륨으로 주입한다 (.dockerignore가 properties를 제외).
# 따라서 이 이미지 안에는 DB 비밀번호·OAuth 시크릿·JWT 키가 들어 있지 않다.
ENV SPRING_PROFILES_ACTIVE=prod \
    SPRING_CONFIG_ADDITIONAL_LOCATION=/config/

# 운영에서는 SQL 출력과 Spring Security DEBUG 로그를 끈다.
# /config 의 공통 설정이 개발용 값(show-sql=true, security DEBUG)을 그대로 가지고 있어도,
# 환경 변수가 설정 파일보다 우선하므로 여기서 덮어쓴다. (jar 안의 application-prod 는 바깥 공통 설정보다 약해서 못 덮는다)
# ddl-auto 는 그대로 둔다: 스키마 변경 도구(Flyway 등)가 없어 새 열이 update 로만 생기고,
# validate 로 바꾸면 열 타입이 조금만 달라도 서버가 안 뜬다. 운영 DB 를 직접 맞춰 본 뒤 바꾼다.
ENV SPRING_JPA_SHOW_SQL=false \
    LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=INFO

# 컨테이너 메모리 한도를 JVM이 인식하게 한다. 작은 컨테이너에서는 SerialGC가 오버헤드가 가장 적다.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=50 -XX:+UseSerialGC -XX:MaxMetaspaceSize=192m"

# curl 설치 없이 확인한다. jammy에 포함된 bash의 /dev/tcp를 쓴다.
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD bash -c ':> /dev/tcp/127.0.0.1/8081'

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
