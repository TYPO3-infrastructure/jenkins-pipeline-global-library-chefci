package org.typo3.chefci.v2.stages

abstract class AbstractStage implements Stage {

    def stageName
    def script

    AbstractStage(script, String stageName) {
        this.script = script
        this.stageName = stageName
    }

    abstract void execute()

}
