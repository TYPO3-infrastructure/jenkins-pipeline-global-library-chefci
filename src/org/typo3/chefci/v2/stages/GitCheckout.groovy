package org.typo3.chefci.v2.stages

class GitCheckout extends AbstractStage {

    GitCheckout(Object script, String stageName) {
        super(script, stageName)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            script.node {
                script.checkout(script.scm)
                // we e.g. have a .kitchen.docker.yml left from the last run. Remove that.
                script.sh("git clean -fdx")
            }
        }
    }

}
