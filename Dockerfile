FROM openjdk:11-jre
# Debian stable no longer has openjdk-8-jre available
# As there is no packages anymore it is probably better
# to use a Docker Hub image to get any security patches

MAINTAINER eduid-dev <eduid-dev@SEGATE.SUNET.SE>

VOLUME ["/opt/eduid/eduid-navet-service/etc", "/opt/eduid/eduid-navet-service/run"]

ADD docker/setup.sh /opt/eduid/setup.sh
RUN /opt/eduid/setup.sh

ADD target/eduid-navet-service-0.1-SNAPSHOT.jar /opt/eduid/eduid-navet-service-0.1-SNAPSHOT.jar

ADD docker/start.sh /start.sh

WORKDIR /

EXPOSE 8080

CMD ["bash", "/start.sh"]
