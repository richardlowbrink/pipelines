node {
    stage('Clone repoA') {
        // Get some code from a GitHub repository
        git 'ssh://git@github.com/lurwas/grpc_richard.git'
    }
    String DoxyConfigFileName = "doxygen_config.dox";
    echo "Doxy Config File Name: $DoxyConfigFileName"
    stage('Generate Doxygen Config File') {
        // Generate the doxygen config file
        sh "doxygen -s -g $DoxyConfigFileName"
    }
    stage('Adjust Config File'){
        echo "Set src in INPUT"
        sh "sed -i 's/INPUT            *=\$/INPUT                  = \"src\"/g' ${DoxyConfigFileName}"
        echo "Generate HTML"
        sh "sed -i 's/GENERATE_HTML[ \t]*= NO/GENERATE_HTML          = YES/g' ${DoxyConfigFileName}"
        echo "Don't generate LATEX"
        sh "sed -i 's/GENERATE_LATEX[ \t]*= YES/GENERATE_LATEX    = NO/g' ${DoxyConfigFileName}"
        echo "Enable the recursive operation"
        sh "sed -i 's/^RECURSIVE[ \t]*= NO/RECURSIVE              = YES/g' ${DoxyConfigFileName}"
    }
    stage('Run DoxyGen'){
         sh "doxygen $DoxyConfigFileName"
    }
    String DoxyGenTarFileName = "doc.tar.gz";
    stage('Package Result'){
        sh "tar -czvf $DoxyGenTarFileName html"
    }
    stage('Archive It'){
        archiveArtifacts "${DoxyGenTarFileName}"
    }
}
