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
        // Assigns false to a job parameter ENABLE_TEST_STAGE
        binding.setVariable('ENABLE_TEST_STAGE', 'false')
        // Defines the previous execution status
        binding.getVariable('currentBuild').previousBuild = [result: 'UNSTABLE']
        helper.registerAllowedMethod('git', [LinkedHashMap]) { args -> return 'Not important' }
        helper.registerAllowedMethod('git', [String]) { args -> return 'Not important' }
        helper.registerAllowedMethod('script', [Closure]) { args -> return 'Not important' }
    }

    // Test the pipeline script for syntax errors
    @Test
    void runPipelineB() {
        setUp()
        loadScript('pipelineB.groovy').run()
        assertJobStatusSuccess()
    }

    // Test the pipeline script for syntax errors
    @Test
    void runPipelineC() {
        setUp()
        loadScript('pipelineC.groovy')
        assertJobStatusSuccess()
    }

    // Define a test method that will run your unit test
    @Test
    void testRunDoxygen() {
        setUp()
        String doxygenConfigFilename = "doxygen_config.dox"

        helper.addShMock('doxygen',  doxygenConfigFilename, 0)
        loadScript('pipeline.groovy').runDoxygen(doxygenConfigFilename)
        assertJobStatusSuccess()
    }

    @Test
    void testGenerateDoxygenConfigFile() {
        setUp()
        String doxygenConfigFilename = "doxygen_config.dox"
        helper.addShMock('doxygen',  "-s g ${doxygenConfigFilename}", 0)
        loadScript('pipeline.groovy').generateDoxygenConfigFile(doxygenConfigFilename)
        assertJobStatusSuccess()
    }

    @Test
    void testPushToPipelinesRepo() {
        setUp()
        String pipelinesRepoURL = 'ssh://foo.bar.com'
        String branch = 'taskB'
        String pipelineName = 'pipelineName'
        String artifactFileName = 'doc.tar.gz'
        helper.registerAllowedMethod('git', [Map.class], { Map m ->
            assert m.url == pipelinesRepoURL
            assert m.branch == branch
        })
        helper.addShMock('git',  "status", 0)
        helper.addShMock('ls', ' -alh', 0)
        helper.addShMock('ls', ' -alh ../', 0)
        helper.addShMock('ls', "../$artifactFileName", 0)
        helper.addShMock('mv', "-f ../$artifactFileName $artifactFileName", 0)
        helper.addShMock('ls', "-alh $artifactFileName", 0)
        helper.addShMock('git', "add $artifactFileName", 0)
        helper.addShMock('git', "commit --allow-empty -m \"add built artifact from pipeline: $pipelineName\"", 0)
        helper.addShMock('git', 'remote set-url origin $pipelinesRepoUrl', 0)
        helper.addShMock('git', 'remote -v', 0)
        helper.addShMock('git', 'status', 0)
        helper.addShMock('git', "push --set-upstream origin $branch", 0)
        helper.addShMock('git',  "push ${pipelinesRepoURL} ${branch}", 0)
        helper.addShMock('git',  "add ${artifactFileName}", 0)
        helper.addShMock('git',  "commit -m \"${pipelineName}\"", 0)
        helper.addShMock('git',  "remote set-url origin ${pipelinesRepoURL}", 0)
        helper.addShMock('git',  "remote -v", 0)

        loadScript('pipeline.groovy').pushToPipelinesRepo(
                pipelinesRepoURL,
                branch,
                pipelineName,
                artifactFileName)
        printCallStack()
        assertJobStatusSuccess()
    }

    @Test
    void testAdjustDoxygenConfigFile() {
        setUp()
        String doxygenConfigFilename = "doxygen_config.dox"
        String sedCommand = "sed -i 's/GENERATE_HTML.*/GENERATE_HTML = YES/' ${doxygenConfigFilename}"
        helper.addShMock('sed',  sedCommand, 0)
        loadScript('pipeline.groovy').adjustDoxygenConfigFile(doxygenConfigFilename, sedCommand)
        assertJobStatusSuccess()
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

    @Test
    void testHasFileBeenModifiedWithoutChange() {
        setUp()
        String filename = "foo.bar"
        helper.addShMock("git  status") { script ->
            String status = "On branch staging\n" +
                    "nothing to commit, working tree clean"
            return [stdout: status, exitValue: 0]
        }
        boolean result = loadScript('pipeline.groovy').isFileModified(filename)
        assertFalse(result, "The file $filename should NOT be modified")
        assertJobStatusSuccess()
    }
}
