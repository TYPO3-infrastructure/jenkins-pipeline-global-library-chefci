package org.typo3.chefci.v2.stages

import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack

class Build extends AbstractStage {

    Build(Object script, String stageName, JenkinsHelper jenkinsHelper, Slack slack) {
        super(script, stageName, jenkinsHelper, slack)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            berkshelf()
        }
    }

    private def berkshelf(){
        script.node {
            script.sh('berks install')
        }
    }
}
