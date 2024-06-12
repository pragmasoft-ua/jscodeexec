FROM openjdk:21
LABEL authors="Oleksii_Drabchak"

VOLUME /app
EXPOSE 8081
COPY target/graalvmdemo-0.0.1-SNAPSHOT.jar jscodeexec.jar

ENTRYPOINT ["java","-jar","jscodeexec.jar"]