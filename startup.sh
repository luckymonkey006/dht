#!/usr/bin/env sh
jar_name='fengleicn_dht'
cmd="nohup java -Xmx512m -Xms512m -jar target/${jar_name}.jar 1>err.log 2>&1 &"

git pull
mvn package
pid=$( ps -ef | grep startup | grep sh | awk '{print $2}' )
if [ -n "$pid" ]; then
    kill -9 ${pid}
fi
pid=$( ps -ef | grep ${jar_name} | grep java | awk '{print $2}' )
if [ -n "$pid" ]; then
    kill -9 ${pid}
fi
${cmd}
while true
do
  sleep 5
  pid=$( ps -ef | grep ${jar_name} | grep java | awk '{print $2}' )
  if [ -z "$pid" ]; then
      ${cmd}
  fi
done &
disown -a