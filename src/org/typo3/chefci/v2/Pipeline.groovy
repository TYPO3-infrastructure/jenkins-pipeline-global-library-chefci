package org.typo3.chefci.v2

import org.typo3.chefci.v2.stages.HelloWorld
import org.typo3.chefci.v2.stages.Stage

class Pipeline implements Serializable {

    def script

    def stages = []

    static Pipeline create(script) {
        // refactor to builder later on
        def pipeline = new Pipeline(script)
        pipeline.addStage(new HelloWorld(pipeline, 'Hello World'))
        return pipeline
        
    }

    private Pipeline(def script) {
        this.script = script
    }

    def execute() {
        script.echo "Hallo Welt!"
    }

    def getScript() {
        return script
    }

    private def addStage(Stage stage) {
        stages << stage
    }

}

