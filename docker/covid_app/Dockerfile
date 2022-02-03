FROM openjdk:17.0-jdk

COPY jar/application.properties application.properties
COPY jar/covid.jar covid.jar

RUN mkdir /covidFile/
RUN chmod -R 777 /covidFile/

ENTRYPOINT ["java", "-Xmx845m", "-Xms845m", "-jar","/covid.jar", "--spring.config.location=application.properties"]
