def pipelineLibrary
def PIPELINE_NAME = "${currentBuild.fullDisplayName}"
def PIPELINE_NUMBER = "${currentBuild.number}"
def DoxygenTarFilename = "doc.tar.gz"
def DoxygenConfigFilename = "doxygen_config.dox"

node() {
    stage('clone pipeline repo') {
        dir('pipeline') {
            git branch: 'main',
                    url: 'ssh://git@github.com/lurwas/pipelines.git'
        }
    }
    stage('load pipeline library') {
        dir('pipeline') {
            pipelineLibrary = load 'pipeline_library.groovy'
        }
    }
    stage('Clone repoA') {
                // Get some code from the GitHub repository containing the clone of grpc
                git 'ssh://git@github.com/lurwas/grpc_richard.git'
    }
    stage('Generate Doxygen Config File') {
        script {
            pipelineLibrary.generateDoxygenConfigFile(DoxygenConfigFilename)
        }
    }
    stage('Adjust Config File') {
        script {
            pipelineLibrary.adjustDoxygenConfigFile(DoxygenConfigFilename, "")
        }
    }
    stage('Run Doxygen') {
        script {
            pipelineLibrary.runDoxygen(DoxygenConfigFilename)
        }
    }
    stage('Package Result') {
        sh "tar -czvf $DoxygenTarFilename html"
    }
    stage('Archive It') {
        archiveArtifacts DoxygenTarFilename
    }
    stage('Push to pipelines repo in branch taskB'){
        script {
            pipelineLibrary.pushToPipelinesRepo("taskB",
                    PIPELINE_NUMBER,
                    PIPELINE_NAME,
                    DoxygenTarFilename)
        }
    }
}
