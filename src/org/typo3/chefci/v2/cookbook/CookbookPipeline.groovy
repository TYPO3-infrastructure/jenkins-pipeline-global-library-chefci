package org.typo3.chefci.v2.cookbook

import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack
import org.typo3.chefci.v2.cookbook.stages.*
import org.typo3.chefci.v2.shared.stages.Stage
import org.jenkinsci.plugins.workflow.cps.DSL

class CookbookPipeline implements Serializable {

    def script

    def stages = []

    DSL steps

    Slack slack

    JenkinsHelper jenkinsHelper

    static builder(script, DSL steps) {
        return new Builder(script, steps)
    }

    static class Builder implements Serializable {

        def stages = []

        def script

        DSL steps

        Slack slack

        JenkinsHelper jenkinsHelper

        Builder(def script, DSL steps) {
            this.script = script
            this.steps = steps
            this.slack = new Slack(script)
            this.jenkinsHelper = new JenkinsHelper(script)
        }

        def withHelloWorldStage() {
            stages << new HelloWorld(script, jenkinsHelper, slack )
            return this
        }

        def withGitCheckoutStage() {
            stages << new GitCheckout(script, jenkinsHelper, slack)
            return this
        }

        def withLintStage() {
            stages << new Lint(script, jenkinsHelper, slack)
            return this
        }

        def withBuildStage() {
            stages << new Build(script, jenkinsHelper, slack)
            return this
        }

        def withAcceptanceStage() {
            stages << new Acceptance(script, jenkinsHelper, slack)
                    .setKitchenLocalYml('.kitchen.docker.yml')
            return this
        }

        def withPublishStage() {
            stages << new Publish(script, jenkinsHelper, slack)
            return this
        }

        def build() {
            return new CookbookPipeline(this)
        }

        def buildDefaultPipeline() {
            withGitCheckoutStage()
            withLintStage()
            withBuildStage()
            withAcceptanceStage()

            if (script.env.BRANCH_NAME == 'master') {
                withPublishStage()
            }

            return new CookbookPipeline(this)
        }

    }

    private CookbookPipeline(Builder builder) {
        this.script = builder.script
        this.stages = builder.stages
        this.steps = builder.steps
        this.slack = builder.slack
        this.jenkinsHelper = builder.jenkinsHelper
    }

    void execute() {
        slack.buildStart()

        // `stages.each { ... }` does not work, see https://issues.jenkins-ci.org/browse/JENKINS-26481
        for (Stage stage : stages) {

            try {
                stage.execute()
            } catch (err) {
                script.currentBuild.result = "FAILURE"
                slack.buildFinish()
                script.error "Build failed: ${err.getMessage()}"
            }
        }

        slack.buildFinish()
    }

}
