FROM openjdk:17.0-jdk

COPY jar/application.properties application.properties
COPY jar/covid.jar covid.jar
ENTRYPOINT ["java","-jar","/covid.jar", "--spring.config.location=application.properties"]
