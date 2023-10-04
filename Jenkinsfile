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

    stage('make image') {
      steps {
        sh 'docker build -t server .'
        sh 'docker image list'
      }
    }

  }
}