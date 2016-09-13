FROM actor/base-java:latest
MAINTAINER Steve Kite <steve@actor.im>

ADD build/docker/bin/* /opt/actor-bots/bin/
ADD build/docker/lib/* /opt/actor-bots/lib/

CMD ["/opt/actor-bots/bin/actor-bots"]