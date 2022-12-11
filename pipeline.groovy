def pushToPipelinesRepo(String pipelinesRepoUrl, String branch, String pipelineName, String artifactFileName) {
    dir("pipelines") {
        git branch: branch, url: pipelinesRepoUrl
        sh 'git status'
        sh 'ls -alh'
        sh 'ls -alh ../'
        sh "ls  ../$artifactFileName"
        sh "cp -f ../$artifactFileName $artifactFileName"
        sh "ls -alh $artifactFileName"
        removedCachedArtifact(artifactFileName, pipelinesRepoUrl, branch)
        sh "git add $artifactFileName"
        sh "git commit -m \"add built artifact from pipeline: $pipelineName\""
        sh "git remote set-url origin $pipelinesRepoUrl"
        sh 'git remote -v'
        sh 'git status'
        sh "git push --set-upstream origin $branch"
    }
}

// Removes the cached git file (if any) from the specified repository and branch
// Violates git best practices. Other solution could be to create a UIN for
// each artifact. But this is a simple solution for now.
def removedCachedArtifact(String artifactFileName, String repoUrl, String branch) {
    try {
        git branch: branch, url: repoUrl
        sh "git rm --cached $artifactFileName"
        sh 'git commit --allow-empty -m "remove cache artifact (if any)"'
        sh "git push --set-upstream origin $branch"
    } catch (Exception ignored) {
        echo "No cached artifact found in $repoUrl:$branch"
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
