package org.typo3.chefci.v2.shared.stages

import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack

abstract class AbstractStage implements Stage {

    def stageName
    def script
    JenkinsHelper jenkinsHelper
    Slack slack

    AbstractStage(script, String stageName, JenkinsHelper jenkinsHelper, Slack slack) {
        this.script = script
        this.stageName = stageName
        this.jenkinsHelper = jenkinsHelper
        this.slack = slack
    }

    abstract void execute()

}
