#!/usr/bin/env sh
jar_name="fengleicn_dht"
cmd="nohup java -Xmx512m -Xms512m -jar target/${jar_name}.jar 1>err.log 2>&1 &"
dont_dead_protect(){
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
    has_old_process="true"
fi

bash -c "${cmd}"
if [[ -n "$has_old_process" ]]; then
    dont_dead_protect
fi
