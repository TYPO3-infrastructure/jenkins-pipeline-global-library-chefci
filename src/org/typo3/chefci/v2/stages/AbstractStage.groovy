package org.typo3.chefci.v2.stages

abstract public class AbstractStage implements Stage {

    def script
    String stageName


    AbstractStage(script, stageName) {
        this.script = script
        this.stageName = stageName
    }

    abstract void run()

    @Override
    void execute() {
        script.stage(stageName) {
            run()
        }
    }

}
