#!/usr/bin/env groovy

package org.typo3.chefci.v1;

def prepare() {
    node {
        // we e.g. have a .kitchen.docker.yml left from the last run. Remove that.
        sh("git clean -fdx")
    }
}


def execute() {
    prepare()

    (new Lint()).execute()
    (new BerkshelfInstall()).execute()
    (new TestKitchen()).execute()
    (new ArchiveArtifacts()).execute()

    if (env.BRANCH_NAME == "master") {
        (new BerkshelfUpload()).execute()
    }
}

return this;