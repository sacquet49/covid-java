#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"

# Maj source
docker stop covid-web_run
git pull origin master

# installation application
# Compilation
docker-compose down -v --rmi all
docker-compose up
cp target/covid-0.0.1-SNAPSHOT.jar docker/covid_app/jar/covid.jar

# Deploiement
cd $SCRIPT_DIR/docker
docker-compose down -v --rmi all
docker-compose up

systemctl restart nginx
