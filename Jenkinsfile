pipeline{
  agent {docker true}
  stages{
    stage("Building"){
      steps{
        sh "mvn build install"
      }
    }
  }
}
