pipeline {
  agent {
    node {
      label 'controller'
    }

  }
  stages {
    stage('build') {
      steps {
        sh '''java --version
'''
        sh '''chmod 777 gradlew
'''
        sh './gradlew clean'
        sh './gradlew build'
      }
    }

    stage('make image') {
      steps {
        sh 'docker build -t server .'
        sh 'docker image list'
      }
    }

  }
}