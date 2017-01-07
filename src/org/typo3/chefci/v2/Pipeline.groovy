package org.typo3.chefci.v2

import org.typo3.chefci.v2.stages.HelloWorld

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

        def build() {
            return new Pipeline(this)
        }

    }

    private Pipeline(Builder builder) {
        this.script = builder.script
        this.stages = builder.stages
        this.steps = builder.steps
    }

    void execute() {
        stages.each {
            it.execute()
        }
    }

}

