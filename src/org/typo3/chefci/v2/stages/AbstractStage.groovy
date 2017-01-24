package org.typo3.chefci.v2.stages

import org.typo3.chefci.helpers.JenkinsHelper

abstract class AbstractStage implements Stage {

    def stageName
    def script
    JenkinsHelper jenkinsHelper

    AbstractStage(script, String stageName) {
        this.script = script
        this.stageName = stageName
        jenkinsHelper = new JenkinsHelper(script)

    }

    abstract void execute()

}
