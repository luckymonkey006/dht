#!/usr/bin/env sh

check_process(){
    if [ "$1" = "" ];
    then
        return 1
    fi

    process_num=`ps -ef | grep "$1" | grep -v "grep" | wc -l`

    if [ $process_num -eq 1 ];
    then
        return 1
    else
        return 0
    fi
}

jar_name='fengleicn_dht'
id='2f12a82'

git pull
mvn package

pid=$( ps -ef | grep ${jar_name} | grep ${id} | awk '{print $2}' )
if [ -n "$pid" ]; then
    kill -9 ${pid}
fi

nohup java -Xmx512m -Xms512m -jar target/${filename}.jar ${id} &>err.log &

#while true
#do
#  sleep 5
#  check_process ${id}
#  if [ $? -eq 0 ]; then
#        nohup java -Xmx512m -Xms512m -jar target/${filename}.jar ${id} &>err.log &
#  fi
#done &>/dev/null &