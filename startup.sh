#!/usr/bin/env sh
jar_name='fengleicn_dht'
id='2f12a82hashcode2f12a82'
cmd="nohup java -Xmx512m -Xms512m -jar target/${jar_name}.jar ${id} &>err.log &"

git pull
mvn package
pid=$( ps -ef | grep ${jar_name} | grep ${id} | awk '{print $2}' )
if [ -n "$pid" ]; then
    kill -9 ${pid}
fi
${cmd}
while true
do
  sleep 5
  pid=$( ps -ef | grep ${jar_name} | grep ${id} | awk '{print $2}' )
  if [ -z "$pid" ]; then
      ${cmd}
  fi
done &
disown -a