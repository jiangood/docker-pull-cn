FROM maven:3-openjdk-17 AS java
WORKDIR /build

ADD pom.xml ./
RUN mvn package -DskipTests -q --fail-never

ADD src src
RUN mvn clean package -DskipTests -q
RUN mv target/app.jar /home/app.jar


FROM amazoncorretto:17
WORKDIR /home
COPY --from=java /home/ ./
EXPOSE 80

ENTRYPOINT ["java","-Duser.timezone=Asia/Shanghai","-jar","/home/app.jar"]
