#!/usr/bin/groovy
def call() {
    return currentBuild.result != "FAILURE"
}