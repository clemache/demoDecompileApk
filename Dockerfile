FROM openjdk:17
EXPOSE 8080
ADD target/decompileApk-0.1.jar decompileApk-0.1.jar
COPY src/tools /app/tools
ENTRYPOINT ["java", "-jar", "/decompileApk-0.1.jar"]
