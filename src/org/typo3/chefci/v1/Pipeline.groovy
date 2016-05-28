#!/usr/bin/env groovy

package org.typo3.chefci.v1;

def prepare() {
    node {
        // we e.g. have a .kitchen.docker.yml left from the last run. Remove that.
        sh("git clean -fdx")
    }
}

def buildResultIsStillGood() {
    return currentBuild.result != "FAILURE"
}

def runIfStillGreen(Object stage){
    if (buildResultIsStillGood()){
        run(stage)
    }
}

def run(Object step){
    step.execute()
}

def execute() {
    this.prepare()

    this.runIfStillGreen(new Lint())

    this.runIfStillGreen(new BerkshelfInstall())
    this.runIfStillGreen(new TestKitchen())
    this.runIfStillGreen(new ArchiveArtifacts())

    if (env.BRANCH_NAME == "master") {
        this.runIfStillGreen(new BerkshelfUpload())
    }

    this.run(new SlackPostBuild())
}

return this;