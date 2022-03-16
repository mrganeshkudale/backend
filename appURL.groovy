def APP_URL

def appURL (Map config = [:]) {

/*
  USAGE:
  helmDeploy(environment: 'nonprod', stage: 'dev' )
    environment       - the kubernetes cluster to deploy to
    namespace         - the namespace to deploy to, optional
    serviceName       - application/service name, used to name the helm release
    stage             - decides which values.yaml to use from helm-release/<STAGE>.yaml, also identifies which namespace to deploy to when namespace not specified
    dockerImageName   - docker image to deploy
    dockerTag         - docker image tag/version to deploy
    dockerRegistry    - docker registry where the image is store
    chart             - chart to use when deploying, defaults to 'tnt-apps/apps-generic-chart'
    helmImage         - the docker image used to run helm commands, defaults to 'nexus-repo.tntad.fedex.com/alpine/helm:2.15.2'
    kubectlImage      - the docker image used to run kubectl commands, defaults to 'nexus-repo.tntad.fedex.com/kubectl:1.16'
*/

  def deployment = [
    environment: (config.environment != null) ? config.environment : (env.DEPLOY_ENVIRONMENT != null) ? env.DEPLOY_ENVIRONMENT : 'nonprod',
    namespace: (config.namespace != null) ? config.namespace : (env.DEPLOY_NAMESPACE != null) ? env.DEPLOY_NAMESPACE : '',
    serviceName: (config.serviceName != null) ? config.serviceName : (env.ARTEFACT_ID != null) ? env.ARTEFACT_ID : '',
    stage: (config.stage != null) ? config.stage : (env.STAGE != null) ? env.STAGE : 'development',
    dockerImageName: (config.dockerImageName != null) ? config.dockerImageName : (env.DOCKER_IMAGE_NAME != null) ? env.DOCKER_IMAGE_NAME : '',
    dockerRegistry: (config.dockerRegistry != null) ? config.dockerRegistry : (env.DOCKER_REGISTRY != null) ? env.DOCKER_REGISTRY : 'nexus-repo.tntad.fedex.com',
    dockerTag: (config.dockerTag != null) ? config.dockerTag : (env.DOCKER_TAG != null) ? env.DOCKER_TAG : '',
    chart: (config.chart != null) ? config.chart : (env.HELM_CHART != null) ? env.HELM_CHART : 'tnt-apps/app-generic',
    helmImage: (config.helmImage != null) ? config.helmImage : (env.HELM_IMAGE != null) ? env.HELM_IMAGE : 'nexus-repo.tntad.fedex.com/alpine/helm:3.1.2',
    kubectlImage: (config.kubectlImage != null) ? config.kubectlImage : (env.KUBECTL_IMAGE != null) ? env.KUBECTL_IMAGE : 'nexus-repo.tntad.fedex.com/kubectl:1.16',
  ]

  // If no namespace is specified then use env.APP + deployment.STAGE
  if ( deployment.namespace == "" ) {
    switch(deployment.stage.toLowerCase()) {
      case ['dev', 'development']:
        deployment.namespace = env.APP.replace("_","-") + "-dev"
        break;
      case ['test', 'release']:
        deployment.namespace = env.APP.replace("_","-") + "-test"
        break;
      case ['prod', 'production']:
        deployment.namespace = env.APP.replace("_","-")
        break;
      default:
        println("Unrecongnised stage '" + deployment.stage + "', not sure which namespace to deploy to."); 
        break; 
    }
  }

  deployment.serviceName = deployment.serviceName + "-" + deployment.stage

  // Grab correct credentials from Jenkins Credentials
  withCredentials([string(credentialsId: "k8s-${deployment.environment}-jenkins-key", variable: 'K8S_HELM_KEY'), string(credentialsId: "k8s-${deployment.environment}-api-host", variable: 'K8S_API_HOST')]) {
    // Kubeconfig path
    env.KUBECONFIG = "${WORKSPACE}/helm-kubeconf/config"

    //  Define Config Template
    def kubeConfig = """
    apiVersion: v1
    kind: Config
    clusters:
    - name: default-cluster
      cluster:
        insecure-skip-tls-verify: true
        server: https://${K8S_API_HOST}
    contexts:
    - name: default-context
      context:
        cluster: default-cluster
        namespace: default
        user: default-user
    current-context: default-context
    users:
    - name: default-user
      user:
        token: ${K8S_HELM_KEY}
    """

    // Save template with assigned variables to kubeconfig file for current job
    templateCmd = """
    #!/bin/sh
    set +x
    mkdir -p ${WORKSPACE}/helm-kubeconf
    echo "${kubeConfig}" > ${env.KUBECONFIG}
    """
    def res = sh(script: templateCmd, returnStdout: true)

    // Output app URL
    APP_URL = docker.image(deployment.kubectlImage).inside('--entrypoint=') {
      sh  label: 'CLICK HERE FOR APPLICATION URL', returnStdout: true, script: 'kubectl get ing --namespace ' + deployment.namespace + ' -l "app.kubernetes.io/name=' + deployment.serviceName + ',helm.sh/release=' + deployment.serviceName + '" -o jsonpath="https://{.items[0].spec.rules[0].host}"'
    }
  }
}
 return this
