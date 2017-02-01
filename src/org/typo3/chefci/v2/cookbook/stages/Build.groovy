package org.typo3.chefci.v2.cookbook.stages

import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack
import org.typo3.chefci.v2.shared.stages.AbstractStage

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

    private berkshelf(){
        script.node {
            script.sh('berks install')
        }
    }
}
