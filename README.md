Assumptions
I assume that the Groovy Sandbox should be enabled.
Since the Groovy Sandbox is enabled, no groovy way of manipulating
the configuration file for doxygen is done, instead sed is used.
A requirement is that GNU sed is installed on the Jenkins machine.
Normaly the Jenkins pipelines would be triggered by a change in the repo using
the 'pollSCM' trigger in the job's configuration.
Now both the pipelineB and pipeLineC perform the jobs and create artifact
although nothing has changed. If the artifact already exists in the git repo
though (and is unchanged from that), the pipeline will succeed, but not push
a 'new' version.
Ways around that would be:
1. Use the 'pollSCM'-trigger above
2. Push a new 'version' of the file, with a naming convention with a combination of BUILD_NUMBER,
   NODE_NAME and EXECUTOR_NUMBER as an example. The downside to it would be that the
   git repo will be full with artifacts with different names but the same content.
3. Force push the artifact to the repo with a combination of git rm --cached <filename>
   (Not suggested since it would violate the git best practices and confuse users)
   
   
Questions and Answers:

Q1: How did you test your pipelines?

A: 

Q2: How did you test repoC python?

A: The python code has 15 different unit tests

Q1: What is the advantage to use LFS?

A: The advantages are reduce the size of the Git repository and make it more efficient to work with.
    LFS also allows uesrs to manage the binary files in the same way they manage
    their source code, using Git command and tools. This can make it easier to track changes
    to the binary files and collaborate with others on projects that include large binary files.
    
Q2: How to adjust this repository to support LFS?

A: Install the LFS extension to Git.
    In the Git repository, type: $> git lfs install.
    Modify the newly created .gitattributes file in the root of the repository to include
    the file patterns that LFS should track.
    Now when a file that matches that pattern is pushed, LFS will automatically take care of storing
    the binary files in the remote LFS repository.
     
     - Alternatives: (all are designed to be easy to use and integrate with existing Git workflows)
       Git LFS, popular open-source tool
       GitHub Large File Storage: Similar tool to Git LFS but integrated into the GitHub platform. Use it
       with any GitHub repository without the need to install and configure any additional tools.
       Git Annex, flexible and powerful
       Git-Media, flexible and powerful
