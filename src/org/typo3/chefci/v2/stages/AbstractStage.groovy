package org.typo3.chefci.v2.stages;

import org.typo3.chefci.v2.Pipeline;

abstract public class AbstractStage implements Stage {

    Pipeline pipeline
    Object script
    String stageName


    AbstractStage(Pipeline pipeline, String stageName) {
        this.pipeline = pipeline
        this.script = pipeline.getScript()
        this.stageName = stageName
    }

    abstract void run();

    def execute() {
        script.stage(stageName) {
            run()
        }
    }

}
