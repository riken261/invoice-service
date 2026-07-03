ARG RUNTIME_IMAGE=harbor.techotakus.cloud/proxy/eclipse-temurin:21-jre

FROM ${RUNTIME_IMAGE} AS extractor
WORKDIR /workspace
COPY build/libs/*-boot.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination /workspace

FROM ${RUNTIME_IMAGE}
WORKDIR /app

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    TZ=Asia/Shanghai \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:InitialRAMPercentage=20.0 -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump.hprof -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8" \
    JAVA_OPTS=""

COPY --from=extractor /workspace/dependencies/ ./
COPY --from=extractor /workspace/spring-boot-loader/ ./
COPY --from=extractor /workspace/snapshot-dependencies/ ./
COPY --from=extractor /workspace/application/ ./

EXPOSE 8080
USER 10001:10001
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
