pipeline {
  agent {
    node {
      label 'backend'
    }

  }
  stages {
    stage('error') {
      steps {
        sh './gradlew build'
      }
    }

  }
}