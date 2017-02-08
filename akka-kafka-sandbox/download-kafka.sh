#!/bin/sh

url="http://www-eu.apache.org/dist/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz"
echo $url
wget -e use_proxy=yes -e http_proxy=http://10.105.198.30:3128 -q "${url}" -O "/tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz"
