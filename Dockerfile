FROM openjdk:17
EXPOSE 8080
ADD target/apkDecompile.jar apkDecompile.jar
ENTRYPOINT ["java", "-jar","apkDecompile.jar"]