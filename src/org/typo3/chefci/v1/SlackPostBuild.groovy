#!/usr/bin/env groovy

package org.typo3.chefci.v1;


def execute(){
    def status = buildResultIsStillGood() ? 'good' : 'bad'
    slackSend(
            message: """
            Cookbook ${env.JOB_BASE_NAME} (${env.BRANCH_NAME}) build ${env.BUILD_NUMBER} *finished*:
            Author: ${env.CHANGE_AUTHOR} / ${env.CHANGE_TITLE}
            Change: ${env.CHANGE_URL}
            Build:  ${env.BUILD_URL}
            """,
            color: status,
            failOnError: false
    )
}

def buildResultIsStillGood(){
	return currentBuild.result != 'FAILURE'
}

return this;