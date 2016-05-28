#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def message = config.message

    def messageColor = "\033[31m"
    def messageColorReset = "\033[0m"

    currentBuild.result = buildResult
    echo "I could notify some things now..."
    error(messageColor + message + messageColorReset)

}