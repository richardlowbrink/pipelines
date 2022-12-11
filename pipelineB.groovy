pipeline {
    def PipelineLibrary = load 'PipelineLibrary.groovy'
    agent any
    environment {
        String PIPELINE_NAME = "${currentBuild.fullDisplayName}"
        String PIPELINE_NUMBER = "${currentBuild.number}"
        String DoxygenTarFilename = "doc.tar.gz"
        String DoxygenConfigFilename = "doxygen_config.dox"
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
                PipelineLibrary.generateDoxygenConfigFile(DoxygenConfigFilename)
            }
        }
        stage('Adjust Config File') {
            steps {
                PipelineLibrary.adjustConfigFile(DoxygenConfigFilename, "")
            }
        }
        stage('Run DoxyGen') {
            steps {
                PipelineLibrary.runDoxygen(DoxygenConfigFilename)
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
                PipelineLibrary.pushToPipelinesRepo("taskB",
                        PIPELINE_NUMBER,
                        PIPELINE_NAME,
                        DoxygenTarFilename,
                        "doxygen_doc.tar.gz")
            }
        }
    }
}
