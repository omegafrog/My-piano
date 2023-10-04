pipeline {
  agent {
    node {
      label 'controller'
    }

  }
  stages {
    stage('build') {
      steps {
        sh '''sudo mkdir ./src/main/resources
sudo cp /home/ubuntu/application.properties ./src/main/resources/application.properties'''
        sh '''chmod 777 gradlew
'''
        sh './gradlew clean'
        sh './gradlew build'
      }
    }

    stage('make image') {
      steps {
        sh '''sudo cp /home/ubuntu/Dockerfile ./Dockerfile
sudo docker build -t server ./ --build-arg PWD=`pwd`'''
        sh 'sudo docker image list'
      }
    }

    stage('deploy') {
      steps {
        sh 'sudo docker tag server jiwoo2211/mypiano:$VERSION'
        sh 'sudo docker push jiwoo2211/mypiano:0.0.1'
        sh 'sudo docker rmi $(docker images -q) '
      }
    }

    stage('clean') {
      steps {
        cleanWs(cleanWhenAborted: true, cleanWhenFailure: true, cleanWhenNotBuilt: true, cleanWhenSuccess: true)
      }
    }

  }
  environment {
    VERSION = '0.0.1'
  }
}