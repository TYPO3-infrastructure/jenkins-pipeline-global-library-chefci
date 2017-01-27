package org.typo3.chefci.v2.stages

import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack

class Lint extends AbstractStage {

    Lint(Object script, String stageName, JenkinsHelper jenkinsHelper, Slack slack) {
        super(script, stageName, jenkinsHelper, slack)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            foodcritic()
            rubocop()
        }
    }

    private def foodcritic(){
        script.node {
            script.sh('foodcritic .')
        }
    }

    private def rubocop(){
        script.node {
            // see also http://atomic-penguin.github.io/blog/2014/04/29/stupid-jenkins-and-chef-tricks-part-1-rubocop/
            script.sh('rubocop --fail-level E')
            script.step([$class: 'WarningsPublisher', canComputeNew: false, canResolveRelativePaths: false, consoleParsers: [[parserName: 'Foodcritic'], [parserName: 'Rubocop']], defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', unHealthy: ''])
            script.step([$class: 'AnalysisPublisher'])
        }
    }

}
