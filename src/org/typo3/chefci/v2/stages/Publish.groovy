package org.typo3.chefci.v2.stages

import hudson.model.ChoiceParameterDefinition
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException
import org.jenkinsci.plugins.workflow.support.steps.input.Rejection

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

    protected publish() {
        def userInput = true
        def didTimeout = false
        def versionPart

        // see https://go.cloudbees.com/docs/support-kb-articles/CloudBees-Jenkins-Enterprise/Pipeline---How-to-add-an-input-step,-with-timeout,-that-continues-if-timeout-is-reached,-using-a-default-value.html
        try {
            script.timeout(time: 15, unit: 'SECONDS') {
                def choice = new ChoiceParameterDefinition('Version Part:', ['patch', 'minor', 'major'] as String[], '')
                versionPart = script.input message: 'Bump major, minor or patch version?', parameters: [choice]
            }
        } catch (FlowInterruptedException err) { // error means we reached timeout
            // err.getCauses() returns [org.jenkinsci.plugins.workflow.support.steps.input.Rejection]
            Rejection rejection = err.getCauses().first()
            if ('SYSTEM' == rejection.getUser().toString()) { // user == SYSTEM means timeout.
                didTimeout = true
            } else {
                userInput = false
                script.echo rejection.getShortDescription()
            }
        }

        script.node {
            def currentBuild = script.currentBuild
            if (didTimeout) {
                currentBuild.displayName = "#${currentBuild.getNumber()} (no upload)"
                script.echo "No cookbook upload was triggered within timeout"
            } else if (userInput == true) {
                // TODO get rid of `bundle install`
                script.sh 'chef exec bundle install'
                // TODO make thor-scmversion globally accessible and get rid of `Thorfile`
                // TODO see http://stackoverflow.com/questions/41474735/use-global-thorfile/41474996
                script.sh "chef exec thor version:bump ${versionPart}"
                def newVersion = script.readFile('VERSION')
                // TODO enable Jenkins to push to Github
                // sh("git push origin ${newVersion}")
                // TODO remove comment once we've finished this...
                //script.sh("berks upload")
                script.echo "Could upload now"
                currentBuild.displayName = "#${currentBuild.getNumber()} - ${newVersion} (${versionPart})"
            } else {
                currentBuild.displayName = "#${currentBuild.getNumber()} (no upload)"
                script.echo "No cookbook upload was triggered within timeout"
            }
        }
    }

}