#!/usr/bin/env groovy

package org.typo3.chefci.v1;

def setup() {
    stage('clone cookbook')
    node {
        checkout(scm)
        // we e.g. have a .kitchen.docker.yml left from the last run. Remove that.
        sh("git clean -fdx")
    }
}


def execute() {
    setup()

    (new Lint()).execute()
    (new BerkshelfInstall()).execute()
    (new TestKitchen()).execute()
    (new BerkshelfUpload()).execute()
}

return this;