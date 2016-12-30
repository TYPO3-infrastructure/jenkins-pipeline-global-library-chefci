#!/usr/bin/env groovy

package org.typo3.chefci.v1;


def execute(){
    slackSend(
            message: "${env.JOB_NAME} build #${env.BUILD_NUMBER} *started*:\nCause: ${currentBuild.rawBuild.getCauses()}\n  ${env.BUILD_URL}",
            failOnError: false
    )
}

return this;