FROM archlinux:base

# At the moment, Gradle is not compatible with Java 20

RUN pacman -Syuu --noconfirm git jre17-openjdk-headless
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk
