FROM eclipse-temurin:19
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/libs/ktor-native-server-0.1-all.jar /app/ktor.jar
ENTRYPOINT ["java","-jar","/app/ktor.jar"]