def pushToPipelinesRepo(String pipelinesRepoUrl, String branch, String pipelineName, String artifactFileName) {
    dir("pipelines") {
        git branch: branch, url: pipelinesRepoUrl
        sh 'git status'
        sh 'ls -alh'
        sh 'ls -alh ../'
        sh "ls  ../$artifactFileName"
        sh "mv -f ../$artifactFileName $artifactFileName"
        // Only git commit add and push if the file has changed
        if (isFileModified(artifactFileName)) {
            sh "git add $artifactFileName"
            sh "git commit -m \"add built artifact from pipeline: $pipelineName\""
            sh "git remote set-url origin $pipelinesRepoUrl"
            sh 'git remote -v'
            sh 'git status'
            sh "git push --set-upstream origin $branch"
        } else {
            echo "No changes to $artifactFileName, will stubbornly refuse to push"
        }
    }
}

def cloneRepoA() {
    dir("repoA") {
        git 'ssh://git@github.com/lurwas/repoA.git'
    }
}

// Return true if the file has been modified in the git repo, false otherwise
boolean isFileModified(String filename) {
    def diff = sh (
            script: "git diff $filename",
            returnStdout: true
    ).trim()
    def regex = /${filename}/
    if (diff =~ regex) {
        println(filename + " has changed")
        return true
    } else {
        println(filename + " has NOT changed")
        return false
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
    sh "doxygen $doxygenConfigFilename"
}

def generateDoxygenConfigFile(String doxygenConfigFilename) {
    echo "Generate Doxygen config file and produce $doxygenConfigFilename"
    sh "doxygen -s -g $doxygenConfigFilename"
}

return this
