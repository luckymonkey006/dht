#!/usr/bin/env bash
workspace='result'
filename='torrentFinder'
git pull
mvn package
mkdir ${workspace} -p
cp target/${filename}.jar ${workspace}
cd ${workspace}
pid=$( ps -ef | grep ${filename} | grep jar | awk '{print $2}' )
if [ -n "$pid" ]; then
    kill -9 ${pid}
fi
nohup java -Xmx1000m -Xms1000m -jar ${filename}.jar &>err.log &