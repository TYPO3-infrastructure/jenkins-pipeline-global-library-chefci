package org.typo3.chefci.v2

import org.typo3.chefci.v2.stages.HelloWorld
import org.typo3.chefci.v2.stages.Lint

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

        def withLintStage() {
            stages << new Lint(script, 'Linting')
        }

        def build() {
            return new Pipeline(this)
        }

        def buildDefaultPipeline() {
            withLintStage()
            return new Pipeline(this)
        }

    }

    private Pipeline(Builder builder) {
        this.script = builder.script
        this.stages = builder.stages
        this.steps = builder.steps
    }

    void execute() {
        prepare()
        stages.each {
            it.execute()
        }
    }

    // TODO maybe this should be a stage on its own?
    private prepare() {
        script.node {
            script.checkout(scm)
            // we e.g. have a .kitchen.docker.yml left from the last run. Remove that.
            script.sh("git clean -fdx")
        }
    }
}

