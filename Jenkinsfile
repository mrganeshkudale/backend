// Single Reference Pipeline
@Library('jenkins-shared-library') _
def previousStageStatus = true
def prodDeployment = false
pipeline {
  agent { label 'linux' }
  options {
    timeout(time: 60, unit: 'MINUTES')
  }
  // Initialize common environment 
  environment {
    SQ_BREAK_BUILD="false"                                         
    BUILD_TYPE='gradle'                                            
    AD_GROUP="azure_tnt_qcoe"
  }
  // Stage for init., build, image creation.
  stages {
 	stage("Build"){
      steps{
        script{
          try{
            initEnv()
            buildApp(gradleCmd: 'gradle clean test build')
            generateReport('build/reports/tests/test', 'index.html', 'Unit Test Report')
            writeDockerfile()
            dockerBuild()
          }catch(error){
            sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' status ${error.toString()}", "FAILED")
          }
        }
      }
	  }
    stage("Scanning"){
      parallel {
        stage('Sonarqube Scan') {
          steps {
            script{
              try{
                sonarqubeScan(exclusions:'**/test-*/**')
              }catch(error){
                sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' Status - ${error.toString()}", "FAILED")
              }
            }
          }
        }
        stage("Docker Scan by Nexus IQ") {
          steps {
            script{
              try{
                dockerScan()
              }catch(error){
                 sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' Status - ${error.toString()}", "FAILED")
              }
            }
          }
        }
        stage("Trivy Scan of Container Image") {
          steps {
            script{
              try{
                 trivyScan()
              }catch(error){
                 sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' Status - ${error.toString()}", "FAILED")
              }
            }
          }
        }
        stage('Artifact Scan by Nexus IQ') {
          steps {
            script{
              try{
                nexusScan()
              }catch(error){
                sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' Status - ${error.toString()}", "FAILED")
              }
            }
          }
        }
      }
    }
    stage('Publish Container to ACR Registry') {
      steps {
        script{
          try{
            dockerPush()
          }catch(error){
            sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' Status - ${error.toString()}", "FAILED")
          }
        }
      }
    }
    stage('Deploy to DEV.') {
      when { expression { env.BRANCH_NAME == 'one-step-promotion' } }
      steps {
        script{
          try{
            helmDeploy(environment: 'tnt-001-nonprod', stage: 'development')
            sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' Status", "SUCCESS")
          }catch(error){
            previousStageStatus = false
            sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' Status", "FAILED")
          }
        }
      }
    }
    stage('Acceptance Tests'){
      agent {
        docker { 
          image "nexus-repo.tntad.fedex.com/jenkins/gradle:jdk8_6.5.1"
          args '-v /etc/hosts:/etc/hosts'
        }
      } 
      steps{
        script{
          try{
            if( env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == 'uat' || env.BRANCH_NAME == 'prod' || env.BRANCH_NAME == 'one-step-promotion' ){
              sh 'gradle junit4Test -Pcategory=SanityTest'
            }else{
              String[] feature = env.BRANCH_NAME.split('-');
              sh "gradle junit4Test -Pcategory=${feature[0]}"
            }
            generateReport('build/reports/tests/junit4Test', 'index.html', 'Acceptance Test Report')
            sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' Status", "SUCCESS")
          }catch(Throwable e){
            generateReport('build/reports/tests/junit4Test', 'index.html', 'Acceptance Test Report')
            sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' Status", "FAILED")
          }
        }
      }
    }
    stage("DAST") {
      agent { dockerfile true }
      steps {
        script{
          try{
            sh '''
            # zap-cli quick-scan --self-contained --start-options '-config api.disablekey=true' https://spring-petclinic-angular-development-qcoe-dev.tnt-001.tntnpk.az.fxei.fedex.com
            zap-cli start --start-options '-config api.disablekey=true'
            zap-cli context import ${WORKSPACE}/zapcontext
            zap-cli context list 
            zap-cli open-url "https://spring-petclinic-angular-development-qcoe-dev.tnt-001.tntnpk.az.fxei.fedex.com"
            # zap-cli spider --context-name zapcontext "https://spring-petclinic-angular-development-qcoe-dev.tnt-001.tntnpk.az.fxei.fedex.com"
            # zap-cli active-scan --recursive -c zapcontext "https://spring-petclinic-angular-development-qcoe-dev.tnt-001.tntnpk.az.fxei.fedex.com"
            zap-cli active-scan https://spring-petclinic-angular-development-qcoe-dev.tnt-001.tntnpk.az.fxei.fedex.com/
            zap-cli status
            zap-cli report -o report.html -f html
            zap-cli shutdown
            ls -a
            '''
            nexusArtifactUploader artifacts: [
              [
                artifactId: 'report',
                classifier: '',
                file: 'report.html',
                type: 'html'
              ]
            ], 
            credentialsId: 'nexus-tnt-userpass',
            groupId: 'qcoe/reports/zap/test',
            nexusUrl: 'nexus-repo.tntad.fedex.com',
            nexusVersion: 'nexus3',
            protocol: 'https',
            repository: 'trivy',
            version: 'test'
            generateReport('.', 'report.html', 'DAST Test Report')
            sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' completed successfully", "SUCCESS")
          }catch(error){
             previousStageStatus = false
             sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' completed successfully", "FAILED")
          }
        }
      }
    }
    stage("Deploy to UAT."){
      when { expression { env.BRANCH_NAME == 'one-step-promotion' } }
        steps{
         script{
           try{
             if(previousStageStatus)
               helmDeploy(environment: 'tnt-001-nonprod', stage: 'development', namespace: 'qcoe-monitoring')
             else
               sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' status", "FAILED")
           }catch(error){
             sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' status", "FAILED")
           }
         }
       }
    }
    stage("Production Deployment Approval"){
     when { expression { env.BRANCH_NAME == 'one-step-promotion' } }
     steps{
       script {
         prodDeployment = false
         sendNotification("${env.BRANCH_NAME}", "'Production Deployment Approval Pending' for frontend, please login to Jenkins for inputs - ${env.BUILD_URL}input", "PENDING")
         prodDeployment = input(id: 'Proceed', message: 'Proceed for production deployment?', parameters: [[$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Please confirm you agree with this']])
         echo 'We have received production deployment flag as : ' + prodDeployment     
       }  
     }
    }  
    stage("Deploy to PROD."){
      when { expression { env.BRANCH_NAME == 'one-step-promotion' } }
        steps{
          script{
            try{
              if(prodDeployment){
                 helmDeploy(environment: 'tnt-001-nonprod', stage: 'development', namespace: 'qcoe-perf')
                 sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' status", "SUCCESS")
              }
              else{
                 sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' status", "FAILED")
              }
             }catch(error){
              sendNotification("${env.BRANCH_NAME} : ${env.STAGE_NAME}", "'Application ${env.STAGE_NAME}' status", "FAILED")
            }
          }
        }
      }
    }  
  post {
    always {
      cleanWs()
    }
  }
}
// Common function for report generation
def generateReport(String reportDir, String reportFiles, String reportName) {
	publishHTML([
	  allowMissing : false,
	  alwaysLinkToLastBuild : false,
	  keepAll : true,
	  reportDir : "${reportDir}",
	  reportFiles : "${reportFiles}",
	  reportName : "${reportName}"
	])
}
// Common function for notification
def sendNotification(String tag, String message, String status){
  script{
	office365ConnectorSend webhookUrl: 'https://myfedex.webhook.office.com/webhookb2/df6d608a-251d-4b01-bf88-a4a9161ffded@b945c813-dce6-41f8-8457-5a12c2fe15bf/IncomingWebhook/d409c31a1fc94196ae7b43f2b22b4864/d919f3c3-4d85-4027-b134-46e074dbef36',
	  message: "Notification : Petclinic ${tag} | ${message}",
	  status: "${status}"
  }
}
