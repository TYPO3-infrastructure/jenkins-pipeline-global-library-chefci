package org.typo3.chefci.helpers

import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException
import org.jenkinsci.plugins.workflow.support.steps.input.Rejection
import com.cloudbees.groovy.cps.NonCPS

class JenkinsHelper implements Serializable {
    def steps

    JenkinsHelper(steps) { this.steps = steps }

    /**
     * Generates a path to a temporary file location, ending with {@code path} parameter.
     *
     * @param path path suffix
     * @return path to file inside a temp directory
     */
    @NonCPS
    String createTempLocation(String path) {
        String tmpDir = steps.pwd tmp: true
        return tmpDir + File.separator + new File(path).getName()
    }

    /**
     * Returns the path to a temp location of a script from the global library (resources/ subdirectory)
     *
     * @param srcPath path within the resources/ subdirectory of this repo
     * @param destPath destination path (optional)
     * @return path to local file
     */
    String copyGlobalLibraryScript(String srcPath, String destPath = null) {

        destPath = destPath ?: createTempLocation(srcPath)
        // writeFile does not overwrite, so we delete the file first
        steps.deleteFile destPath
        steps.writeFile file: destPath, text: steps.libraryResource(srcPath)
        steps.echo "copyGlobalLibraryScript: copied ${srcPath} to ${destPath}"
        return destPath
    }

    /**
     * Annotates the build namme (#i by default) with some text, e.g., the version built.
     *
     * @param text Text to add to {@code currentBuild.displayName}
     */
    def annotateBuildName(String text) {
        steps.currentBuild.displayName = "#${steps.currentBuild.getNumber()} ${text}"
    }

    /**
     * Generates a pipeline {@code input} step that times out after a specified amount of time.
     *
     * The options for the timeout are supplied via {@code timeoutOptions}.
     * The options for the input dialog are supplied via {@code inputOptions}.
     *
     * The returned Map contains the following keys:
     *
     * - proceed: true, if the Proceed button was clicked, false if aborted manually aborted or timed out
     * - reason: 'user', if user hit Proceed or Abort; 'timeout' if input dialog timed out
     * - submitter: name of the user that submitted or canceled the dialog
     * - additional keys for every parameter submitted via {@code inputOptions.parameters}
     *
     * @param args Map containing inputOptions and timoutOptions, both passed to respective steps
     * @return Map containing above specified keys response/reason/submitter and those for parameters
     */
    Map inputWithTimeout(Map args) {
        // see https://go.cloudbees.com/docs/support-kb-articles/CloudBees-Jenkins-Enterprise/Pipeline---How-to-add-an-input-step,-with-timeout,-that-continues-if-timeout-is-reached,-using-a-default-value.html
        try {
            steps.timeout(args.timeoutOptions) {
                def inputOptions = args.inputOptions
                inputOptions.submitterParameter = "submitter"

                // as we ask for the submitter, we get a Map back instead of a string
                // besides the parameter supplied using args.inputOptions, this will include "submitter"
                Map responseValues = steps.input args.inputOptions
                steps.echo "Submitted by ${responseValues.submitter}"

                return [proceed: true, reason: 'user'] + responseValues
            }
        } catch (FlowInterruptedException err) { // error means we reached timeout
            // err.getCauses() returns [org.jenkinsci.plugins.workflow.support.steps.input.Rejection]
            Rejection rejection = err.getCauses().first()

            if ('SYSTEM' == rejection.getUser().toString()) { // user == SYSTEM means timeout.
                return [proceed: false, reason: 'timeout']
            } else { // explicitly aborted
                steps.echo rejection.getShortDestepsion()
                return [proceed: false, reason: 'user', submitter: rejection.getUser().toString()]
            }
        }
    }
}
