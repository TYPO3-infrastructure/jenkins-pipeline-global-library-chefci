package org.typo3.chefci.v2.chefrepo

import org.jenkinsci.plugins.workflow.cps.DSL
import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack
import org.typo3.chefci.v2.chefrepo.stages.*
import org.typo3.chefci.v2.shared.stages.Stage

class ChefRepoPipeline implements Serializable {

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

        def build() {
            return new ChefRepoPipeline(this)
        }

        def buildDefaultPipeline() {
            withHelloWorldStage()

            return new ChefRepoPipeline(this)
        }

    }

    private ChefRepoPipeline(Builder builder) {
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
                script.error "Build failed"
            }
        }

        slack.buildFinish()
    }

}
