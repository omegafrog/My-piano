pipeline {
  agent {
    node {
      label 'backend'
    }

  }
  stages {
    stage('error') {
      steps {
        sh '''sudo chmod 777 gradlew
./gradlew build'''
      }
    }

  }
}