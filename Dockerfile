FROM docker.sunet.se/eduid/python3env

MAINTAINER eduid-dev <eduid-dev@SEGATE.SUNET.SE>

VOLUME ["/opt/eduid/eduid-navet-service/etc", "/opt/eduid/eduid-navet-service/run"]

ADD docker/setup.sh /opt/eduid/setup.sh
RUN /opt/eduid/setup.sh

ADD target/eduid-navet-service-0.1-SNAPSHOT.jar /opt/eduid/eduid-navet-service-0.1-SNAPSHOT.jar

ADD docker/start.sh /start.sh

WORKDIR /

EXPOSE 8080

CMD ["bash", "/start.sh"]
