package org.typo3.chefci.v2.stages

class Build extends AbstractStage {

    Build(Object script, String stageName) {
        super(script, stageName)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            berkshelf()
        }
    }

    private def berkshelf(){
        script.node {
            script.sh('berks install')
        }
    }
}
