FROM --platform=linux/amd64 ubuntu
EXPOSE 8080:8080
RUN apt-get update && apt-get install -y libpq-dev
COPY build/bin/linuxX64/releaseExecutable/ktor-native-server.kexe /ktor-native-server
ENTRYPOINT ["/ktor-native-server"]