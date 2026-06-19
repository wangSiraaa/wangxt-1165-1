FROM openjdk:8-jre-alpine

LABEL maintainer="dairyfarm"

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY target/milk-quality-control-1.0.0.jar app.jar

EXPOSE 19465

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
