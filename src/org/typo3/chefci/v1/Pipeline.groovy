#!/usr/bin/env groovy

package org.typo3.chefci.v1;

def prepare() {
    node {
        checkout(scm)
        // we e.g. have a .kitchen.docker.yml left from the last run. Remove that.
        sh("git clean -fdx")
    }
}

def failTheBuild(String message) {
    def messageColor = "\u001B[32m" 
    def messageColorReset = "\u001B[0m"

    currentBuild.result = "FAILURE"
    echo messageColor + message + messageColorReset
    error(message)
}

def preBuildNotify() {
    (new SlackPreBuild()).execute()
}

def run(Object step){
    try {
        step.execute()
    } catch (err) {
        // unfortunately, err.message is not whitelisted by script security
        //failTheBuild(err.message)
        failTheBuild("Build failed")
    }
}

def execute() {
    this.prepare()

    this.run(new Lint())

    this.run(new BerkshelfInstall())
    this.run(new TestKitchen())
    this.run(new ArchiveArtifacts())

    if (env.BRANCH_NAME == "master") {
        this.run(new BerkshelfUpload())
    }
    (new SlackPostBuild()).execute()
}

return this;
