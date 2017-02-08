FROM ubuntu:14.04

#ENV http_proxy http://10.105.198.30:3128
#ENV HTTP_PROXY http://10.105.198.30:3128
#ENV https_proxy http://10.105.198.30:3128
#ENV HTTPS_PROXY http://10.105.198.30:3128

RUN apt-get update
RUN apt-get install -y curl > /dev/null
RUN apt-get install -y erlang-nox > /dev/null

RUN curl -o /tmp/rabbitmq-server_3.2.4-1_all.deb "http://launchpadlibrarian.net/168456687/rabbitmq-server_3.2.4-1_all.deb"
RUN dpkg -i /tmp/rabbitmq-server_3.2.4-1_all.deb

EXPOSE 5672
EXPOSE 15672
EXPOSE 61613

RUN rabbitmq-plugins enable rabbitmq_management
RUN rabbitmq-plugins enable rabbitmq_stomp
RUN service rabbitmq-server stop
