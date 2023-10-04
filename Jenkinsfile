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
        sh 'docker build -t server /home/ubuntu'
        sh 'docker image list'
      }
    }

    stage('clean') {
      steps {
        cleanWs(cleanWhenAborted: true, cleanWhenFailure: true, cleanWhenNotBuilt: true, deleteDirs: true)
      }
    }

  }
}