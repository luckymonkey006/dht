#!/usr/bin/env sh
jar_name='fengleicn_dht'
cmd="nohup java -Xmx512m -Xms512m -jar target/${jar_name}.jar 1>err.log 2>&1 &"

dht_demon_do_while(){
    while true; do
            sleep 5
            pid=$( ps -ef | grep ${jar_name} | grep java | awk '{print $2}' )
            if [[ -z "$pid" ]]; then
                bash -c "${cmd}"
            fi
        done &
    disown -a
}

git pull
mvn package

pid=$(ps -ef | grep ${jar_name} | grep java | awk '{print $2}')
if [[ -n "$pid" ]]; then
    kill -9 ${pid}
fi
bash -c "${cmd}"
echo $( ps -ef | grep startup | grep sh | awk '{print $0}' )
sh_pid_num=$( ps -ef | grep dht_demon_do_while | grep -v grep | awk '{print $2}' | wc -l )
if [[ "$sh_pid_num" -le 1 ]]; then
    dht_demon_do_while
fi
