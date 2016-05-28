#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def buildResult = config.result
    def message = config.message

    // set currentBuild.result
    if (buildResult != null) {
        if (buildResult == "FAILURE" || buildResult == "SUCCESS") {

            def messageColor = (buildResult == "FAILURE") ? "\033[31m" : "\033[32m"
            def messageColorReset = "\033[0m"
            echo messageColor + message + messageColorReset
            
            echo "Updating currentBuild.result to ${buildResult}"
            currentBuild.result = buildResult

            // set the build result
        } else {
            thrown "buildResult has to be one of null|SUCCESS|FAILURE"
        }
    }
}