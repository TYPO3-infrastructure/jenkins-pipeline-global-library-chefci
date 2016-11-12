#!/usr/bin/env groovy

package org.typo3.chefci.v1;


def execute(){
    slackSend(
            message: """
            Cookbook ${env.JOB_BASE_NAME} (${env.BRANCH_NAME}) build ${env.BUILD_NUMBER} *started*:
            Author: ${env.CHANGE_AUTHOR} / ${env.CHANGE_TITLE}
            Change: ${env.CHANGE_URL}
            Build:  ${env.BUILD_URL}
            """,
            failOnError: false
    )
}

return this;