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

    stage('clean') {
      steps {
        cleanWs(cleanWhenAborted: true, cleanWhenFailure: true, cleanWhenNotBuilt: true, deleteDirs: true)
      }
    }

  }
}