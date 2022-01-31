FROM maven:3-openjdk-17

WORKDIR /usr/src

CMD mvn clean install -DskipTests
