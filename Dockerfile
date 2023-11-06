#FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine
FROM --platform=linux/amd64 adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine
ARG arch

ENV TZ=Asia/Tashkent
RUN apk update && apk upgrade && apk add ca-certificates && update-ca-certificates && apk add --update tzdata
RUN rm -rf /var/cache/apk/*

COPY target/app.jar app.jar

ENTRYPOINT ["java","-jar","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:60002","app.jar"]
#docker build -t registry.gitlab.com/project.gov.uz/backend/file-service:lastest .
#docker push registry.gitlab.com/project.gov.uz/backend/file-service:lastest