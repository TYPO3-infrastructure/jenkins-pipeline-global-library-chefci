package org.typo3.chefci.v2.stages

public class HelloWorld extends AbstractStage {

    HelloWorld(script, String stageName) {
        super(script, stageName)
    }

    @Override
    void run() {
        // script.echo "Hello World!"
        printf "hallo welt"
    }

}
