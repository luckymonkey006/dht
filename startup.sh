#!/usr/bin/env sh
filename='fengleicn_dht'

git pull
mvn package

pid=$( ps -ef | grep ${filename} | grep jar | awk '{print $2}' )
if [ -n "$pid" ]; then
    kill -9 ${pid}
fi

nohup sudo java -Xmx512m -Xms512m -jar target/${filename}.jar &>err.log &
