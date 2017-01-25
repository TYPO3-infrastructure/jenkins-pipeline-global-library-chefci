package org.typo3.chefci.v2

import org.typo3.chefci.v2.stages.*

class Pipeline implements Serializable {

    def script

    def stages = []

    def steps

    static builder(script, steps) {
        return new Builder(script, steps)
    }

    static class Builder implements Serializable {

        def stages = []

        def script

        def steps

        Builder(def script, def steps) {
            this.script = script
            this.steps = steps
        }

        def withHelloWorldStage() {
            stages << new HelloWorld(script, 'Hello World')
            return this
        }

        def withGitCheckoutStage() {
            stages << new GitCheckout(script, 'Git Checkout')
            return this
        }

        def withLintStage() {
            stages << new Lint(script, 'Linting')
            return this
        }

        def withBuildStage() {
            stages << new Build(script, 'Build')
            return this
        }

        def withAcceptanceStage() {
            stages << new Acceptance(script, 'Acceptance')
                    .setKitchenLocalYml('.kitchen.docker.yml')
            return this
        }

        def withPublishStage() {
            stages << new Publish(script, 'Publish')
            return this
        }

        def build() {
            return new Pipeline(this)
        }

        def buildDefaultPipeline() {
            withGitCheckoutStage()
            withLintStage()
            withBuildStage()
            withAcceptanceStage()

            if (script.env.BRANCH_NAME == 'master') {
                withPublishStage()
            }

            return new Pipeline(this)
        }

    }

    private Pipeline(Builder builder) {
        this.script = builder.script
        this.stages = builder.stages
        this.steps = builder.steps
    }

    void execute() {
        // `stages.each { ... }` does not work, see https://issues.jenkins-ci.org/browse/JENKINS-26481
        for (Stage stage : stages) {
            stage.execute()
        }
    }

}
