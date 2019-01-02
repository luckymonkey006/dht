#!/usr/bin/env sh
jar_name='fengleicn_dht'
id='2f12a82hashcode2f12a82'
cmd="nohup java -Xmx512m -Xms512m -jar target/${filename}.jar ${id} &>err.log &"
get_pid="ps -ef | grep ${jar_name} | grep ${id} | awk '{print $2}'"

git pull
mvn package
pid=$(${get_pid})
if [ -n "$pid" ]; then
    kill -9 ${pid}
fi
${cmd}
while true
do
  sleep 5
  pid=$(${get_pid})
  if [ -z "$pid" ]; then
      ${cmd}
  fi
done &
disown -a