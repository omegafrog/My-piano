pipeline {
  agent {
    node {
      label 'backend'
    }

  }
  stages {
    stage('build') {
      steps {
        sh '''chmod 777 gradlew
'''
        sh './gradlew clean'
        sh './gradlew build'
      }
    }

    stage('run') {
      steps {
        sh '''arr=($(ls build/libs | grep .jar))
FILENAME=${arr[-1]}
echo ${FILENAME}


nohup java -jar build/libs/${FILENAME} --spring.config.location=file:/home/ubuntu/application.properties >./log.txt 2>&1 &'''
      }
    }

  }
}