pipeline {
  agent {
    node {
      label 'backend'
    }

  }
  stages {
    stage('error') {
      steps {
        sh '''chmod 777 gradlew
./gradlew build'''
      }
    }

  }
}