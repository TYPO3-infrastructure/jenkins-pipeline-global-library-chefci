package org.typo3.chefci.v2.cookbook.stages

import hudson.model.ChoiceParameterDefinition
import org.jenkinsci.plugins.credentialsbinding.impl.CredentialNotFoundException
import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack
import org.typo3.chefci.v2.shared.stages.AbstractStage

class Publish extends AbstractStage {
    def versionIncrements = ['patch', 'minor', 'major']

    Publish(Object script, JenkinsHelper jenkinsHelper, Slack slack) {
        super(script, 'Publish', jenkinsHelper, slack)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            publish()
        }
    }

    /**
     * Determines next version and uploads the cookbook
     */
    protected publish() {

        // one out of patch/minor/major
        String increment

        // try to parse the commit message for a hint
        script.node {
            increment = parseCommitMessageForVersionIncrementHint()
        }

        // ask the user. We cannot do this within a node{} block as it blocks an executor.
        if (!increment) {
            slack.notifyVersionBump()
            // TODO currently, anonymous users can submit dialogs. This might be restricted using the `submitter` parameter of the `input` step.
            def userInput = getInput()
            increment = userInput.versionIncrement
        }

        // any of the two said patch/minor/major
        if (increment) {
            def newVersion
            script.node {
                // make sure this node has the correct workspace content (not given automatically :-/)
                script.unstash 'cookbook'
                // increase the version by the specified increment
                newVersion = bumpVersion(increment)
                // upload the cookbook
                upload()
            }
            // set the build name to something meaningful, i.e.
            // #1 - 1.2.3 (patch)
            jenkinsHelper.annotateBuildName(" - ${newVersion} (${increment})")

        } else {
            script.echo "No cookbook upload"
            jenkinsHelper.annotateBuildName("(no upload)")
        }
    }

    /**
     * Checks the last commit message subject for an occurrence of #patch, #minor or #major
     * and returns the version increment found.
     *
     * @return Version increment found in the subject, null otherwise
     */
    protected String parseCommitMessageForVersionIncrementHint() {

        // TODO this could instead use the global variable changeSets
        String commitSubject = script.gitCommitSubject()
        for (String increment : versionIncrements) {
            // look for #patch, #minor, #major
            if (commitSubject.contains('#' + increment)) {
                script.echo "Found hint to increase version in the commit subject: ${increment}"
                return increment
            }
        }
        script.echo "Did not find a hint to increment version in the commit subject."
        null
    }

    /**
     * Asks the user for his/her opinion, if we should bump the version number and then upload this.
     *
     *  this will return something like
     *  [response: true, reason:user, submitter:johndoe, versionIncrement:patch]
     *  [response: false, reason:timeout]
     *
     * @return Map response options
     */
    protected Map getInput() {
        // generate the input dialog for version bump.
        // contains the choices patch/minor/major.
        // timeouts out after specified time.
        def choice = new ChoiceParameterDefinition('versionIncrement', versionIncrements as String[], 'Specify the level to increase the version.')
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

        // we assume that such credential entry exist
        def credentialsId = 'github-token'
        try {
            script.withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: credentialsId, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {
                // this was the coolest way to not store the password that I found
                // http://stackoverflow.com/questions/33570075/tag-a-repo-from-a-jenkins-workflow-script
                // (there is a warning "warning: invalid credential line: get", but doesn't matter)
                // yes, using HTTPS, because we have an API token already!
                script.sh "git config credential.username ${script.env.GIT_USERNAME}"
                script.sh "git config credential.helper '!echo password=\$GIT_PASSWORD; echo'"
                script.sh "GIT_ASKPASS=true git push origin ${newVersion}"
            }
        } catch (CredentialNotFoundException e) {
            script.error "Credential entry not found: ${e.getMessage()}"
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