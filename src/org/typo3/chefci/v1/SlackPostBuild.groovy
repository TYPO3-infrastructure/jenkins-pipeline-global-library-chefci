#!/usr/bin/env groovy

package org.typo3.chefci.v1;


def execute(){
    def status = buildResultIsStillGood() ? 'green' : 'red'
    slackSend(
            message: "Build Started - ${env.JOB_NAME}: ${env.BUILD_NUMBER}",
            color: status,
            failOnError: false
    )
}

def buildResultIsStillGood(){
	return currentBuild.result != 'FAILURE'
}

return this;