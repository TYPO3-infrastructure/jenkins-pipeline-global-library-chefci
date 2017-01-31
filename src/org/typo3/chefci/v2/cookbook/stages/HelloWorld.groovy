package org.typo3.chefci.v2.cookbook.stages

import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack
import org.typo3.chefci.v2.shared.stages.AbstractStage

public class HelloWorld extends AbstractStage {

    HelloWorld(Object script, JenkinsHelper jenkinsHelper, Slack slack) {
        super(script, 'Hello World', jenkinsHelper, slack)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            script.echo "Hiho!"
        }
    }

}
