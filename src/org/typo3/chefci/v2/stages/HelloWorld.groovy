package org.typo3.chefci.v2.stages

public class HelloWorld implements Stage {

    def script
    String stageName

    HelloWorld(script, String stageName) {
        this.script = script
        this.stageName = stageName
    }

    @Override
    void execute() {
        script.stage(stageName) {
            script.echo "Hiho!"
        }
    }

}
