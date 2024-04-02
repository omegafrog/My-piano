#!/bin/bash

REPOSITORY=/home/ec2-user/build
APP_NAME=My-piano

echo "빌드 파일 복사"

cp $REPOSITORY/zip/*.jar $REPOSITORY/

echo "애플리케이션 pid 확인"

CURRENT_PID=$(pgrep -fl $APP_NAME | awk '{print $1}')

mkdir $REPOSITORY/config

aws s3 cp --recursive s3://mypiano-deploy/config $REPOSITORY/config


if [ -z "$CURRENT_PID" ]; then
  echo "구동 중인 app이 없음"
else
  echo "kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

echo "새 애플리케이션 배포"

JAR_NAME=$(ls -tr $REPOSITORY/build/libs/*.jar | tail -n 1)
sudo chmod +x $JAR_NAME

aws s3 cp s3://mypiano-deploy/certs/http_ca.crt ./http_ca.crt
sudo keytool -import -trustcacerts -keystore /usr/lib/jvm/java-17-amazon-corretto.x86_64/lib/security/cacerts -storepass changeit -noprompt -alias elasticCA -file ~/build/http_ca.crt
sleep 5

nohup java -jar \
  -Dspring.config.location=$REPOSITORY/config/application-prod.properties \
  -Dspring.profiles.active=prod \
  $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &



