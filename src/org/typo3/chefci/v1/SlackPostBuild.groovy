#!/usr/bin/env groovy

package org.typo3.chefci.v1;


def execute(){
    def status = buildResultIsStillGood() ? 'good' : 'bad'
    slackSend(
            message: "{env.JOB_NAME} build #${env.BUILD_NUMBER} *finished*:\n  {env.BUILD_URL}
            """,
            color: status,
            failOnError: false
    )
}

def buildResultIsStillGood(){
	return currentBuild.result != 'FAILURE'
}

return this;