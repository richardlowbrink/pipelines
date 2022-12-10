pipeline {
    agent any
    environment {
        PIPELINE_NAME = "${currentBuild.fullDisplayName}"
        PIPELINE_NUMBER = "${currentBuild.number}"
        COMMIT_AUTHOR_NAME = "lurwas"
        COMMIT_AUTHOR_EMAIL = "lurwas@emacs.se"
        DoxyConfigFileName = "doxygen_config.dox"
        DoxyWarningLogFileName = "doxygen_warning.log"
        CsvOutputFileName = "repoC/log_lines.csv"
        WarningLogFile = "/var/lib/jenkins/workspace/pipelineC/doxygen_warning.log"
    }
    stages {
        stage('Clone Repository') {
            steps {
                // Get some code from a GitHub repository
                git 'ssh://git@github.com/lurwas/grpc_richard.git'
            }
        }
        stage('Generate Doxygen Config File') {
            steps {
                // Generate the doxygen config file
                sh "doxygen -s -g $DoxyConfigFileName"
            }
        }
        stage('Adjust Config File'){
            steps {
                echo "Set src in INPUT"
                sh "sed -i 's/INPUT            *=\$/INPUT                  = \"src\"/g' ${DoxyConfigFileName}"
                echo "Generate HTML"
                sh "sed -i 's/GENERATE_HTML[ \t]*= NO/GENERATE_HTML          = YES/g' ${DoxyConfigFileName}"
                echo "Don't generate LATEX"
                sh "sed -i 's/GENERATE_LATEX[ \t]*= YES/GENERATE_LATEX    = NO/g' ${DoxyConfigFileName}"
                echo "Enable the recursive operation"
                sh "sed -i 's/^RECURSIVE[ \t]*= NO/RECURSIVE              = YES/g' ${DoxyConfigFileName}"
                sh "sed -i 's/^WARN_LOGFILE[ \t]*=/WARN_LOGFILE              = ${DoxyWarningLogFileName}/g' ${DoxyConfigFileName}"
            }
        }
        stage('Run DoxyGen'){
            steps {
                sh "doxygen $DoxyConfigFileName"
                sh "ls -alh $DoxyWarningLogFileName"
            }
        }
        stage('Clone RepoC'){
            steps {
                sh 'mkdir -p repoC'
                dir("repoC")
                        {
                            git branch: 'main',
                                    url: 'ssh://git@github.com/lurwas/repoC.git'
                            sh 'pwd'
                            sh 'ls -alh src/'
                            sh 'ls -alh'
                            sh 'pip install -e .'
                            sh "python3 src/log_parser_richard/__init__.py -f ${WarningLogFile}"
                        }
            }
        }
        stage('Archive It'){
            steps {
                archiveArtifacts "${CsvOutputFileName}"
            }
        }
        stage('Push to pipelines repo in branch taskC'){
            steps {
                sh 'mkdir -p pipelines'
                dir("pipelines"){
                    git branch: 'taskC', url: 'ssh://git@github.com/lurwas/pipelines.git'
                    sh 'ls -alh ../repoC'
                    sh 'find ../repoC -name log_lines.csv'
                    sh 'git config user.name "${COMMIT_AUTHOR_NAME}"'
                    sh 'git config user.email "${COMMIT_AUTHOR_EMAIL}"'
                    sh 'git status'
                    sh 'git checkout -B taskC'
                    sh 'git fetch --all'
                    sh 'git reset --hard origin/taskC'
                    sh 'ls  ../repoC/log_lines.csv'
                    sh 'cp -f ../repoC/log_lines.csv log_lines"${PIPELINE_NUMBER}".csv'
                    sh 'ls -alh log_lines"${PIPELINE_NUMBER}.csv"'
                    sh 'git add log_lines"${PIPELINE_NUMBER}.csv"'
                    sh 'git commit -m "Added built artifact from pipeline: ${PIPELINE_NAME}:${PIPELINE_NUMBER}"'
                    sh 'git remote set-url origin git@github.com:lurwas/pipelines.git'
                    sh 'git remote -v'
                    sh 'git status'
                    sh 'git push --set-upstream origin taskC'
                    sh 'git push'
                }
            }
        }
    }
}
