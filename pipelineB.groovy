def pipelineLibrary
String pipelineName = "${currentBuild.fullDisplayName}"
String doxygenTarFilename = "doc.tar.gz"
String doxygenConfigFilename = "doxygen_config.dox"
String pipelinesRepoURL = 'ssh://git@github.com/richardlowbrink/pipelines.git'

node() {
    // Check that sed is installed and output the version
    stage('Sanity Check') {
        sh 'sed --version'
    }
    stage('Clone Pipeline Repository and Load Pipeline Library') {
        dir('pipelines') {
            git branch: 'main',
                    url: pipelinesRepoURL
            pipelineLibrary = load 'pipeline.groovy'
        }
    }
    stage('Clone repoA') {
        pipelineLibrary.cloneRepoA()
    }
    stage('Generate Doxygen Config File') {
        script {
            pipelineLibrary.generateDoxygenConfigFile(doxygenConfigFilename)
        }
    }
    stage('Adjust Config File') {
        script {
            pipelineLibrary.adjustDoxygenConfigFile(doxygenConfigFilename, "")
        }
    }
    stage('Run Doxygen') {
        script {
            pipelineLibrary.runDoxygen(doxygenConfigFilename)
        }
    }
    stage('Package Result') {
        sh "tar -czvf $doxygenTarFilename html"
    }
    stage('Archive It') {
        archiveArtifacts doxygenTarFilename
    }
    stage('Push to pipelines repo in branch taskB') {
        script {
            pipelineLibrary.pushToPipelinesRepo(
                    pipelinesRepoURL,
                    "taskB",
                    pipelineName,
                    doxygenTarFilename)
        }
    }
}
