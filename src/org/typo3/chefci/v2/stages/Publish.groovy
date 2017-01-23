package org.typo3.chefci.v2.stages

import hudson.model.ChoiceParameterDefinition
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException
import org.jenkinsci.plugins.workflow.support.steps.input.Rejection
import org.typo3.chefci.helpers.JenkinsHelper

class Publish extends AbstractStage {

    Publish(Object script, String stageName) {
        super(script, stageName)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            publish()
        }
    }

    /**
     * Generates a pipeline {@code input} step that times out after a specified amount of time.
     *
     * The options for the timeout are supplied via {@code timeoutOptions}.
     * The options for the input dialog are supplied via {@code inputOptions}.
     *
     * The returned Map contains the following keys:
     *
     * - response: true, if response was received, false if input manually canceled or timed out
     * - reason: 'user', if user hit Proceed or Cancel; 'timeout' if input dialog timed out
     * - submitter: name of the user that submitted or canceled the dialog
     * - additional keys for every parameter submitted via {@code inputOptions.parameters}
     *
     * @param args Map containing inputOptions and timoutOptions, both passed to respective steps
     * @return Map containing above specified keys response/reason/submitter and those for parameters
     */
    protected Map inputWithTimeout(Map args) {
        // see https://go.cloudbees.com/docs/support-kb-articles/CloudBees-Jenkins-Enterprise/Pipeline---How-to-add-an-input-step,-with-timeout,-that-continues-if-timeout-is-reached,-using-a-default-value.html
        try {
            script.timeout(args.timeoutOptions) {
                def inputOptions = args.inputOptions
                inputOptions.submitterParameter = "submitter"

                // as we ask for the submitter, we get a Map back instead of a string
                // besides the parameter supplied using args.inputOptions, this will include "submitter"
                Map responseValues = script.input args.inputOptions
                script.echo "Submitted by ${responseValues.submitter}"

                return [response: true, reason: 'user'] + responseValues
            }
        } catch (FlowInterruptedException err) { // error means we reached timeout
            // err.getCauses() returns [org.jenkinsci.plugins.workflow.support.steps.input.Rejection]
            Rejection rejection = err.getCauses().first()

            if ('SYSTEM' == rejection.getUser().toString()) { // user == SYSTEM means timeout.
                return [response: false, reason: 'timeout']
            } else { // explicitly canceled
                script.echo rejection.getShortDescription()
                return [response: true, reason: 'user', submitter: rejection.getUser()]
            }
        }
    }

    protected publish() {
        def userInput = true
        def didTimeout = false
        def versionPart

        // generate the input dialog for version bump.
        // contains the choices patch/minor/major.
        // timeouts out after specified time.
        def choice = new ChoiceParameterDefinition('versionBump', ['patch', 'minor', 'major'] as String[], 'Version Part:')
        def inputOptions = [message: 'Bump major, minor or patch version?', parameters: [choice]]
        def timeoutOptions = [time: 15, unit: 'SECONDS']

        // call the input dialog
        // this will return something like
        // [response: true, reason:user, submitter:johndoe, versionBump:patch]
        // [response: false, reason:timeout]
        Map input = inputWithTimeout([inputOptions: inputOptions, timeoutOptions: timeoutOptions])


        script.echo "Got input ${input}"

        script.node {
            def jenkinsHelper = new JenkinsHelper(script)

            // if we don't get any user response, we do nothing
            if (input.response) {
                // TODO get rid of `bundle install`
                script.sh 'chef exec bundle install'
                // TODO make thor-scmversion globally accessible and get rid of `Thorfile`
                // TODO see http://stackoverflow.com/questions/41474735/use-global-thorfile/41474996
                script.sh "chef exec thor version:bump ${input.versionBump}"
                def newVersion = script.readFile('VERSION')
                // TODO enable Jenkins to push to Github
                // sh("git push origin ${newVersion}")
                // TODO remove comment once we've finished this...
                //script.sh("berks upload")
                script.echo "Could upload now"
                jenkinsHelper.annotateBuildName(" - ${newVersion} (${input.versionBump})")
            } else {
                jenkinsHelper.annotateBuildName("(no upload)")
                script.echo "No cookbook upload was triggered within timeout"
            }
        }
    }

}