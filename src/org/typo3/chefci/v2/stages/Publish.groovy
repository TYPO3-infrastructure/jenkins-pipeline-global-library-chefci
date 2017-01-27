package org.typo3.chefci.v2.stages

import hudson.model.ChoiceParameterDefinition
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
     * Asks the user whether the cookbook should be uploaded
     */
    protected publish() {

        def userInput = getInput()

        def newVersion
        // if we don't get any user response, we do nothing
        if (userInput.proceed) {
            script.node {
                newVersion = bumpVersion(userInput.versionBump)
                upload()
            }
            // set the build name to something meaningful, i.e.
            // #1 - 1.2.3 (patch)
            jenkinsHelper.annotateBuildName(" - ${newVersion} (${userInput.versionBump})")

        } else {
            script.echo "No cookbook upload was triggered within timeout"
            jenkinsHelper.annotateBuildName("(no upload)")
        }
    }

    /**
     * Asks the user for his/her opinion, if we should bump the version number and then upload this.
     *
     *  this will return something like
     *  [response: true, reason:user, submitter:johndoe, versionBump:patch]
     *  [response: false, reason:timeout]
     *
     * @return Map response options
     */
    protected Map getInput() {
        // generate the input dialog for version bump.
        // contains the choices patch/minor/major.
        // timeouts out after specified time.
        def choice = new ChoiceParameterDefinition('versionBump', ['patch', 'minor', 'major'] as String[], 'Version Part:')
        def inputOptions = [message: 'Bump major, minor or patch version?', parameters: [choice]]
        def timeoutOptions = [time: 1, unit: 'DAYS']

        // call the input dialog
        Map inputValues = jenkinsHelper.inputWithTimeout([inputOptions: inputOptions, timeoutOptions: timeoutOptions])

        script.echo "Got input ${inputValues}"

        inputValues
    }

    /**
     * Increases the version number by level {@code level} using thor-scmversion and pushes git tag.
     *
     * @param level patch/minor/major
     * @return new version number
     */
    protected String bumpVersion(String level) {
        jenkinsHelper.copyGlobalLibraryScript 'cookbook/Gemfile', 'Gemfile'
        jenkinsHelper.copyGlobalLibraryScript 'cookbook/Thorfile', 'Thorfile'
        // TODO get rid of `bundle install`
        script.sh 'chef exec bundle install'
        // TODO make thor-scmversion globally accessible and get rid of `Thorfile`
        // TODO see http://stackoverflow.com/questions/41474735/use-global-thorfile/41474996
        script.sh "chef exec thor version:bump ${level}"
        def newVersion = script.readFile('VERSION')

        // TODO
        def credentialsId = 'github-chefcitypo3org-token'
        script.withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: credentialsId, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {
            // this was the coolest way to not store the password that I found
            // http://stackoverflow.com/questions/33570075/tag-a-repo-from-a-jenkins-workflow-script
            // (there is a warning "warning: invalid credential line: get", but doesn't matter)
            // yes, using HTTPS, because we have an API token already!
            script.sh("git config credential.username ${script.env.GIT_USERNAME}")
            script.sh("git config credential.helper '!echo password=\$GIT_PASSWORD; echo'")
            script.sh("GIT_ASKPASS=true git push origin ${newVersion}")
        }

        newVersion
    }

    /**
     * Uploads the cookbook
     *
     * @param userInput
     * @return
     */
    protected upload() {
        script.sh("berks upload")
    }
}