package org.typo3.chefci.v2.stages

import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack

public class HelloWorld extends AbstractStage {

    HelloWorld(Object script, String stageName, JenkinsHelper jenkinsHelper, Slack slack) {
        super(script, stageName, jenkinsHelper, slack)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            script.echo "Hiho!"
        }
    }

}
