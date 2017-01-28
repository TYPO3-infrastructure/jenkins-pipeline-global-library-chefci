package org.typo3.chefci.v2.stages

import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack

class Build extends AbstractStage {

    Build(Object script,  JenkinsHelper jenkinsHelper, Slack slack) {
        super(script, 'Build', jenkinsHelper, slack)
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
