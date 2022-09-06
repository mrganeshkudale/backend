pipeline{
  agent {dockerfile true}
  stages{
    stage("Building"){
      steps{
        sh "mvn build"
      }
    }
  }
}
