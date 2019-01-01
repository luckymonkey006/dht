#!/usr/bin/env sh
jar_name='fengleicn_dht'
id='2f12a82'

git pull
mvn package

pid=$( ps -ef | grep ${jar_name} | grep ${id} | awk '{print $2}' )
if [ -n "$pid" ]; then
    kill -9 ${pid}
fi

nohup java -Xmx512m -Xms512m -jar target/${filename}.jar ${id} &>err.log &

while(1){
  sleep 5
  pid=$( ps -ef | grep ${jar_name} | grep ${id} | awk '{print $2}' )
  if [ -z "$pid" ]; then
        nohup java -Xmx512m -Xms512m -jar target/${filename}.jar ${id} &>err.log &
  fi
}
