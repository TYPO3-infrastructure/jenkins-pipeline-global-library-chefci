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

    protected inputWithTimeout(Map args) {
        def response = null
        def reason = null
        // see https://go.cloudbees.com/docs/support-kb-articles/CloudBees-Jenkins-Enterprise/Pipeline---How-to-add-an-input-step,-with-timeout,-that-continues-if-timeout-is-reached,-using-a-default-value.html
        try {
            script.timeout(args.timeoutOptions) {
                def inputOptions = args.inputOptions
                inputOptions.submitterParameter = "submitter"

                response = script.input args.inputOptions
                script.echo "Submitted by ${response.submitter}"
                reason = response.submitter
            }
        } catch (FlowInterruptedException err) { // error means we reached timeout
            // err.getCauses() returns [org.jenkinsci.plugins.workflow.support.steps.input.Rejection]
            Rejection rejection = err.getCauses().first()
            if ('SYSTEM' == rejection.getUser().toString()) { // user == SYSTEM means timeout.
                reason = 'timeout'
            } else {
                reason = 'user'
                script.echo rejection.getShortDescription()
            }
        }
        response + [reason: reason]
    }

    protected publish() {
        def userInput = true
        def didTimeout = false
        def versionPart

        // generate the input dialog for version bump.
        // contains the choices patch/minor/major.
        // timeouts out after specified time.
        def choice = new ChoiceParameterDefinition('version', ['patch', 'minor', 'major'] as String[], 'Version Part:')
        def inputOptions = [message: 'Bump major, minor or patch version?', parameters: [choice]]
        def timeoutOptions = [time: 15, unit: 'SECONDS']

        // call the input dialog
        Map input = inputWithTimeout([inputOptions: inputOptions, timeoutOptions: timeoutOptions])

        println "Got input ${input}"

//        script.node {
//            def jenkinsHelper = new JenkinsHelper(script)
//
//            if (inputValues.version) {
//                // TODO get rid of `bundle install`
//                script.sh 'chef exec bundle install'
//                // TODO make thor-scmversion globally accessible and get rid of `Thorfile`
//                // TODO see http://stackoverflow.com/questions/41474735/use-global-thorfile/41474996
//                script.sh "chef exec thor version:bump ${versionPart}"
//                def newVersion = script.readFile('VERSION')
//                // TODO enable Jenkins to push to Github
//                // sh("git push origin ${newVersion}")
//                // TODO remove comment once we've finished this...
//                //script.sh("berks upload")
//                script.echo "Could upload now"
//                jenkinsHelper.annotateBuildName(" - ${newVersion} (${versionPart})")
//            } else {
//                jenkinsHelper.annotateBuildName("(no upload)")
//                script.echo "No cookbook upload was triggered within timeout"
//            }
//        }
    }

}