#!/bin/bash

REPOSITORY=/home/ec2-user/build
APP_NAME=springboot-webservice

echo "빌드 파일 복사"

cp $REPOSITORY/zip/*.jar $REPOSITORY/

echo "애플리케이션 pid 확인"

CURRENT_PID=$(pgrep -fl springboot-webservice | grep jar | awk '{print $1}')



if [ -z "$CURRENT_PID" ]; then
  echo "구동 중인 app이 없음"
else
  echo "kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

echo "새 애플리케이션 배포"

JAR_NAME=$(ls -tr $REPOSITORY/build/libs/*.jar | tail -n 1)
#chmod +x $JAR_NAME
#
#aws s3 cp --recursive s3://mypiano-deploy/config $REPOSITORY/config/

nohup java -jar \
  -Dspring.config.location=$REPOSITORY/config/application-mysql.properties \
  -Dspring.profiles.active=mysql \
  $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &



