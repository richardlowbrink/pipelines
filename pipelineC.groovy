def pipelineLibrary
String pipelineName = "${currentBuild.fullDisplayName}"
String doxygenConfigFilename = "doxygen_config.dox"
String doxygenWarningLogFileName = "doxygen_warning.log"
String pipelinesRepoURL = 'ssh://git@github.com/lurwas/pipelines.git'
String csvOutputFilename = "log_lines.csv"
node() {
    stage('Clone Pipeline Repository and Load Pipeline Library') {
        dir('pipelines') {
            git branch: 'main',
                    url: pipelinesRepoURL
            pipelineLibrary = load 'pipeline.groovy'
        }
    }
    stage('Clone Repository') {
        // Get the code from the grpc cloned repo
        git 'ssh://git@github.com/lurwas/grpc_richard.git'
    }
    stage('Generate Doxygen Config File') {
        pipelineLibrary.generateDoxygenConfigFile(doxygenConfigFilename)
    }
    stage('Adjust Config File'){
        pipelineLibrary.adjustDoxygenConfigFile(doxygenConfigFilename, doxygenWarningLogFileName)
    }
    stage('Run Doxygen'){
        pipelineLibrary.runDoxygen(doxygenConfigFilename)
        // Verify that the doxygen warning log file exists
        sh "ls -alh $doxygenWarningLogFileName"
    }
    stage('Clone RepoC'){
        dir("repoC") {
            git branch: 'main',
                    url: 'ssh://git@github.com/lurwas/repoC.git'
            sh 'pwd'
            sh 'ls -alh src/'
            sh 'ls -alh'
            sh 'pip install -e .'
            sh "python3 src/log_parser_richard/__init__.py -f ../$doxygenWarningLogFileName"
            sh "mv -f $csvOutputFilename ../$csvOutputFilename"
        }
    }
    stage('Archive It'){
        archiveArtifacts "${csvOutputFilename}"
    }
    stage('Push to Pipelines Repository in Branch taskC') {
        pipelineLibrary.pushToPipelinesRepo(
                pipelinesRepoURL,
                "taskC",
                pipelineName,
                csvOutputFilename)
    }
}
