#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()


    def buildResult = config.buildResult

    // set currentBuild.result
    if (buildResult != null) {
        if (buildResult == "FAILURE" || buildResult == "SUCCESS") {
            // set the build result
            currentBuild.result = buildResult
        } else {
            thrown "buildResult has to be one of null|SUCCESS|FAILURE"
        }
    }
}