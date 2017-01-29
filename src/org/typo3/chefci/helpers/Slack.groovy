package org.typo3.chefci.helpers

class Slack implements Serializable {
    def script

    JenkinsHelper jenkinsHelper

    Slack(script) {
        this.script = script
        jenkinsHelper = new JenkinsHelper(script)
    }

    def send(String message) {
        send message: message
    }

    def send(Map args) {
        script.slackSend(
                args + [failOnError: false]
        )
    }

    def buildStart() {
        send "${script.env.JOB_NAME} build #${script.env.BUILD_NUMBER} *started*:\n${jenkinsHelper.getBuildCauses()}\n${script.env.BUILD_URL}"
    }

    def buildFinish() {
        def status = (script.currentBuild.result != 'FAILURE') ? 'good' : 'danger'

        send(
                message: "${script.env.JOB_NAME} build #${script.env.BUILD_NUMBER} *finished*:\n${script.env.BUILD_URL}",
                color: status,
        )
    }

    def notifyVersionBump() {
        send(
                message: "${script.env.JOB_NAME} build #${script.env.BUILD_NUMBER} *waiting for input*:\n${script.env.JOB_URL}",
                color: 'warning',
        )
    }
}
