FROM node:lts

RUN apt-get update && \
	apt-get install -y --no-install-recommends default-jdk chromium

ENV CHROME_BIN=chromium
ENV CHROMIUM_FLAGS="--no-sandbox"
