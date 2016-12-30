#!/usr/bin/env groovy

package org.typo3.chefci.v1;


def execute(){
    slackSend(
            message: "${env.JOB_NAME} build #${env.BUILD_NUMBER} *started*:\n${getCauses()}\n  ${env.BUILD_URL}",
            failOnError: false
    )
}

@NonCPS
def getCauses() {
    def causes = currentBuild.rawBuild.getCauses()
    return causes.collect{ it.getShortDescription() }.join(" / ")
}

return this;