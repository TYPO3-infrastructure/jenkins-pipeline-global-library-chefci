#!/usr/bin/env groovy

package org.typo3.chefci.v1;


def execute(){
    try {
        slackSend(
                message: "${env.JOB_NAME} build #${env.BUILD_NUMBER} *started*:\n  ${env.BUILD_URL}",
                failOnError: false
        )
    } catch(e) {
        echo "Slack failed, I don't care.."
    }
}

return this;