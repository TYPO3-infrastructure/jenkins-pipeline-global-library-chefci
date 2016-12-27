#!/usr/bin/env groovy

package org.typo3.chefci.v1;


def execute(){
    def status = buildResultIsStillGood() ? 'good' : 'danger'

    try {
        slackSend(
                message: "${env.JOB_NAME} build #${env.BUILD_NUMBER} *finished*:\n  ${env.BUILD_URL}",
                color: status,
                failOnError: false
        )
    } catch(e) {
        echo "Slack failed, I don't care.."
    }
}

def buildResultIsStillGood(){
    return currentBuild.result != 'FAILURE'
}

return this;