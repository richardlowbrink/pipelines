### Assumptions
I assume that the Groovy Sandbox should be enabled.
Since the Groovy Sandbox is enabled, no groovy way of manipulating
the configuration file for doxygen is done, instead sed is used.
A requirement is that GNU sed is installed on the Jenkins machine.
I assume that the naming 'artifacts' is used for the artifacts created by the
pipeline jobs and not the pipeline jobs themselves.
The assumption has also been made that the repoB and repoC should contain artifacts
from my latest build of the pipeline jobs. If you want to build your own artifacts from pipelineC
and push them, you need to remove the 'artifact' from the taskC branch.
Another assumptions are although no shared libraries are to be used, I took
the liberty to break out common code into a shared groovy file for inclusion in both
pipeline scripts. (I like dry code).
Normally the Jenkins pipelines would be triggered by a change in the repo using
the 'pollSCM' trigger in the job's configuration.
As it is now both the pipelineB and pipeLineC perform the jobs and create artifacts
although nothing has changed. The artifact in pipelineB is not possible to perform
a git diff on since it's a binary file (and I don't want to have Enterprise GitHub as a dependency).
The artifact pipelineC is a CSV file and can be diffed. The artifact in pipelineC will ONLY be 
pushed to the repo if there's a change in the CSV file.
Ways around that would be:
1. Use the 'pollSCM'-trigger above
2. Use Git LFS to store the artifact in pipelineB. In the pipelineB case it would be a great benefit 
   to use LFS since the artifact is a binary file and if it's stored using Git LFS the git diff command 
   will be able to show the difference and git will be able to track the file as a 'normal' git file.
   Example for pipelineB:
   ```git lfs track "*.tar.gz"```
3. Push a new 'version' of the file, with a naming convention with a combination of BUILD_NUMBER,
   NODE_NAME and EXECUTOR_NUMBER as an example. The downside to it would be that the
   git repo will be full of artifacts with different names but the same content.
4. Force push the artifact to the repo with a combination of git rm --cached <filename>
   (Not suggested since it would violate the git best practices and confuse users)
   
   
### Questions and Answers:

#### Q1: How did you test your pipelines?

A: I tested the pipelineB and pipelineC by running them in a staging environment.
   The syntax of the files was tested with the lint using the Jenkins CLI command
   and with some editing in the IntelliJ IDE.
   I also made use of the Replay feature in Jenkins to test the pipelines.
   I also did some unit testing using the Jenkins Pipeline Unit library.
   The unit tests were done in the IntelliJ IDE and is located in the test folder of the pipeline repo main branch
   in the file TestPipeline.groovy.
   The pom.xml file is located in the repo and the tests are possible to run using the command
   ```mvn test```
   Example test here:

```groovy
import org.junit.jupiter.api.Test
// Import the pipelineUnit library
import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assertions.assertFalse
// Everything tested in this class since I'm not really testing classes
class TestPipeline extends BasePipelineTest {
@Override
@BeforeEach
void setUp() {
super.setUp()
binding.setVariable('ENABLE_TEST_STAGE', 'false')
binding.getVariable('currentBuild').previousBuild = [result: 'UNSTABLE']
}
    @Test
    void testIsFileModifiedWithModifiedFile() {
        setUp()
        String filename = "foo.bar"
        helper.addShMock("") { throw new Exception('Unexpected sh call') }
        helper.addShMock("git status") { script ->
            String status = "On branch foo\n" +
                    "Changes to be committed:\n  " +
                    "(use \"git reset HEAD <file>...\" to unstage)" +
                    "\n\n\tmodified:   $filename"
            return [stdout: status, exitValue: 0]
        }
        boolean result = loadScript('pipeline.groovy').isFileModified(filename)
        assertTrue(result, "The file $filename should be modified")
        assertJobStatusSuccess()
    }
    // ...
}
```

### Q2: How did you test repoC python?

A: The python code has 15 different unit tests. I also ran pylint, flake8  and coverage on the code. 
    I also performed some manual testing as well as debugged the Python code with the use of Pycharm.

### Q1: What is the advantage to use LFS?

A: The advantages are reduce the size of the Git repository and make it more efficient to work with.
    LFS also allows users to manage the binary files in the same way they manage
    their source code, using Git command and tools. This can make it easier to track changes
    to the binary files and collaborate with others on projects that include large binary files.
    As mentioned in the assumptions above, the artifact in pipelineB is a binary file and the git tracking
    of the file is not possible, but using LFS it would be possible to track the file as a 'normal' git file.
    
### Q2: How to adjust this repository to support LFS?

A: Install the LFS extension to Git.
    Modify the newly created .gitattributes file in the root of the repository to include
    the file patterns that LFS should track.
    In the Git repository, example for the repo (git lfs install only needed once for a system):
```console
git lfs install
git lfs track "*.tar.gz"
git add .gitattributes
git commit -m "add .gitattributes"
git push origin feature_track
```
Now when a file that matches that pattern is pushed, LFS will automatically take care of storing
the binary files in the remote LFS repository.
More information here: https://git-lfs.github.com/
     
- Alternatives: (all are designed to be easy to use and integrate with existing Git workflows)
  Git LFS, popular open-source tool
  GitHub Large File Storage: Similar tool to Git LFS but integrated into the GitHub platform. Use it
  with any GitHub repository without the need to install and configure any additional tools.
  Git Annex, flexible and powerful
  Git-Media, flexible and powerful
