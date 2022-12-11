import static PipelineLibrary.*

pipeline {
    agent any
    environment {
        def PIPELINE_NAME = "${currentBuild.fullDisplayName}"
        def PIPELINE_NUMBER = "${currentBuild.number}"
        def DoxygenTarFilename = "doc.tar.gz"
        def DoxygenConfigFilename = "doxygen_config.dox"
    }
    stages {
        stage('Clone repoA') {
            steps {
                // Get some code from the GitHub repository containing the clone of grpc
                git 'ssh://git@github.com/lurwas/grpc_richard.git'
            }
        }
        stage('Generate Doxygen Config File') {
            steps {
                script {
                    generateDoxygenConfigFile(DoxygenConfigFilename)
                }
            }
        }
        stage('Adjust Config File') {
            steps {
                script {
                    adjustDoxygenConfigFile(DoxygenConfigFilename, "")
                }
            }
        }
        stage('Run DoxyGen') {
            steps {
                script {
                    runDoxygen(DoxygenConfigFilename)
                }
            }
        }
        stage('Package Result') {
            steps {
                sh "tar -czvf $DoxygenTarFilename html"
            }
        }
        stage('Archive It') {
            steps {
                archiveArtifacts "${DoxyGenTarFileName}"
            }
        }
        stage('Push to pipelines repo in branch taskB'){
            steps {
                script {
                    pushToPipelinesRepo("taskB",
                            PIPELINE_NUMBER,
                            PIPELINE_NAME,
                            "doxygen_doc.tar.gz")
                }
            }
        }
    }
}
