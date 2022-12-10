pipeline {
    agent any
    environment {
        String DoxyGenTarFileName = "doc.tar.gz"
        String DoxyConfigFileName = "doxygen_config.dox"
    }
    stages {
        stage('Clone repoA') {
            steps {
                // Get some code from a GitHub repository
                git 'ssh://git@github.com/lurwas/grpc_richard.git'
            }
        }
        stage('Generate Doxygen Config File') {
            steps {
                echo "Doxy Config File Name: $DoxyConfigFileName"
                // Generate the doxygen config file
                sh "doxygen -s -g $DoxyConfigFileName"
            }
        }
        stage('Adjust Config File') {
            steps {
                echo "Set src in INPUT"
                sh "sed -i 's/INPUT            *=\$/INPUT                  = \"src\"/g' ${DoxyConfigFileName}"
                echo "Generate HTML"
                sh "sed -i 's/GENERATE_HTML[ \t]*= NO/GENERATE_HTML          = YES/g' ${DoxyConfigFileName}"
                echo "Don't generate LATEX"
                sh "sed -i 's/GENERATE_LATEX[ \t]*= YES/GENERATE_LATEX    = NO/g' ${DoxyConfigFileName}"
                echo "Enable the recursive operation"
                sh "sed -i 's/^RECURSIVE[ \t]*= NO/RECURSIVE              = YES/g' ${DoxyConfigFileName}"
            }
        }
        stage('Run DoxyGen') {
            steps {
                sh "doxygen $DoxyConfigFileName"
            }
        }
        stage('Package Result') {
            steps {
                sh "tar -czvf $DoxyGenTarFileName html"
            }
        }
        stage('Archive It') {
            steps {
                archiveArtifacts "${DoxyGenTarFileName}"
            }
        }
    }
}
