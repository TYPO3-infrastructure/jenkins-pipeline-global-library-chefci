package org.typo3.chefci.v2.stages

import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack

class GitCheckout extends AbstractStage {

    GitCheckout(Object script, String stageName, JenkinsHelper jenkinsHelper, Slack slack) {
        super(script, stageName, jenkinsHelper, slack)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            script.node {
                script.checkout(script.scm)
                // we e.g. have a .kitchen.docker.yml left from the last run. Remove that.
                script.sh("git clean -fdx")
            }
        }
    }

}
