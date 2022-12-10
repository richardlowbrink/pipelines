pipeline {
    agent any
    environment {
        PIPELINE_NAME = "${currentBuild.fullDisplayName}"
        PIPELINE_NUMBER = "${currentBuild.number}"
        COMMIT_AUTHOR_NAME = "lurwas"
        COMMIT_AUTHOR_EMAIL = "lurwas@emacs.se"
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
        stage('Push to pipelines repo in branch taskB'){
            steps {
                sh 'mkdir -p pipelines'
                dir("pipelines"){
                    git branch: 'taskB', url: 'ssh://git@github.com/lurwas/pipelines.git'
                    sh 'git config user.name "${COMMIT_AUTHOR_NAME}"'
                    sh 'git config user.email "${COMMIT_AUTHOR_EMAIL}"'
                    sh 'git status'
                    sh 'git checkout -B taskB'
                    sh 'git fetch --all'
                    sh 'git reset --hard origin/taskB'
                    sh 'ls  ../"${DoxyGenTarFileName}"'
                    sh 'cp -f ../"${DoxyGenTarFileName}" doxy_gen"${PIPELINE_NUMBER}".tar.gz'
                    sh 'ls -alh doxy_gen"${PIPELINE_NUMBER}".tar.gz'
                    sh 'git add doxy_gen"${PIPELINE_NUMBER}".tar.gz'
                    sh 'git commit -m "Added built artifact from pipeline: ${PIPELINE_NAME}:${PIPELINE_NUMBER}"'
                    sh 'git remote set-url origin git@github.com:lurwas/pipelines.git'
                    sh 'git remote -v'
                    sh 'git status'
                    sh 'git push --set-upstream origin taskB'
                    sh 'git push'
                }
            }
        }
    }
}
