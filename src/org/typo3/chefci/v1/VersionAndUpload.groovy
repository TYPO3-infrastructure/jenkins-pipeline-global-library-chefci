#!/usr/bin/env groovy

package org.typo3.chefci.v1;

def execute(){
    this.bumpVersion()
}

protected def bumpVersion(){
    def userInput = true
    def didTimeout = false

    stage ('Version and Upload') {

        // see https://go.cloudbees.com/docs/support-kb-articles/CloudBees-Jenkins-Enterprise/Pipeline---How-to-add-an-input-step,-with-timeout,-that-continues-if-timeout-is-reached,-using-a-default-value.html
        try {
            timeout(time: 15, unit: 'SECONDS') {
                choice = new ChoiceParameterDefinition('Version Part:', ['patch', 'minor', 'major'] as String[], '')
                versionPart = input message: 'Bump major, minor or patch version?', parameters: [choice]
            }
        } catch (err) { // error means we reached timeout
            def user = err.getCauses()[0].getUser()
            if ('SYSTEM' == user.toString()) { // user == SYSTEM means timeout.
                didTimeout = true
            } else {
                userInput = false
                echo "Aborted by: [${user}]"
            }
        }

        node {
            if (didTimeout) {
                currentBuild.displayName = "#${currentBuild.getNumber()} (no upload)"
                echo "No cookbook upload was triggered within timeout"
            } else if (userInput == true) {
                // TODO get rid of `bundle install`
                sh 'chef exec bundle install'
                // TODO make thor-scmversion globally accessible and get rid of `Thorfile`
                // TODO see http://stackoverflow.com/questions/41474735/use-global-thorfile/41474996
                sh "chef exec thor version:bump ${versionPart}"
                def newVersion = readFile('VERSION')
                // TODO enable Jenkins to push to Github
                sh("git push origin ${newVersion}")
                // TODO remove comment once we've finished this...
                //sh("berks upload")
                currentBuild.displayName = "#${currentBuild.getNumber()} - ${newVersion} (${versionPart})"
            } else {
                currentBuild.displayName = "#${currentBuild.getNumber()} (no upload)"
                echo "No cookbook upload was triggered within timeout"
            }
        }
    }
}

return this;