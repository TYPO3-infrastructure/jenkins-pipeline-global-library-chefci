package org.typo3.chefci.v2.stages

import org.typo3.chefci.v2.Pipeline

public class HelloWorld extends AbstractStage {

    HelloWorld(Pipeline pipeline, String stageName) {
        super(pipeline, stageName)
    }

    @Override
    void run() {
        script.echo "Hello World!"
    }

}
