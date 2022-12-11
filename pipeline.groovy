def pushToPipelinesRepo(String branch, String pipelineNumber, String pipelineName, String artifactFileName) {
    // TODO: Remove the hard coded credentials
    String COMMIT_AUTHOR_NAME = "lurwas"
    String COMMIT_AUTHOR_EMAIL = "lurwas@emacs.se"
    String pipelinesRepo = "ssh://git@github.com/lurwas/pipelines.git"
    sh 'mkdir -p pipelines'
    dir("pipelines") {
        git branch: branch, url: pipelinesRepo
        sh "git config user.name $COMMIT_AUTHOR_NAME"
        sh "git config user.email $COMMIT_AUTHOR_EMAIL"
        sh 'git status'
        sh "git checkout -B $branch"
        sh 'git fetch --all'
        sh "git reset --hard origin/$branch"
        sh 'pwd'
        sh 'ls -alh'
        sh 'ls -alh ../'
        sh "ls  ../$artifactFileName"
        String uniqueFileName = "${pipelineName}_${pipelineNumber}_${artifactFileName}"
        sh "cp -f ../$artifactFileName $uniqueFileName"
        sh "ls -alh $uniqueFileName"
        sh "git add $uniqueFileName"
        sh "git commit -m \"add built artifact from pipeline: $pipelineName:$pipelineNumber\""
        sh "git remote set-url origin $pipelinesRepo"
        sh 'git remote -v'
        sh 'git status'
        sh "git push --set-upstream origin $branch"
    }
}

def adjustDoxygenConfigFile(String doxygenConfigFilename, String doxygenWarningLogName) {
    echo "Set src in INPUT"
    sh "sed -i 's/INPUT            *=\$/INPUT                  = \"src\"/g' $doxygenConfigFilename"
    echo "Generate HTML"
    sh "sed -i 's/GENERATE_HTML[ \t]*= NO/GENERATE_HTML          = YES/g' $doxygenConfigFilename"
    echo "Don't generate LATEX"
    sh "sed -i 's/GENERATE_LATEX[ \t]*= YES/GENERATE_LATEX    = NO/g' $doxygenConfigFilename"
    echo "Enable the recursive operation"
    sh "sed -i 's/^RECURSIVE[ \t]*= NO/RECURSIVE              = YES/g' $doxygenConfigFilename"
    if (doxygenWarningLogName.isEmpty())
    {
        echo "No warning log file name given. Not generating warning log."
        return
    }
    sh "sed -i 's/^WARN_LOGFILE[ \t]*=/WARN_LOGFILE              = $doxygenWarningLogName/g' $doxygenConfigFilename"
}

def runDoxygen(String doxygenConfigFilename) {
    echo "Run Doxygen and only produce HTML documentation"
    sh "doxygen ${doxygenConfigFilename}"
}

def generateDoxygenConfigFile(String doxygenConfigFilename) {
    echo "Generate Doxygen config file and produce $doxygenConfigFilename"
    sh "doxygen -s -g $doxygenConfigFilename"
}

return this
