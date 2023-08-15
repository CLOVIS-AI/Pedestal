FROM alpine:latest as builder

COPY app.tar /home/server/app.tar
WORKDIR /home/server
RUN tar -xf app.tar
RUN mkdir -p extracted
RUN mv app-*/* extracted

FROM alpine:latest

RUN apk add --no-cache openjdk17-jre-headless
COPY --from=builder /home/server/extracted /opt/app

WORKDIR /opt/app
EXPOSE 9000
ENTRYPOINT [ "/opt/app/bin/app" ]
