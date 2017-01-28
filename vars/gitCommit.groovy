#!/usr/bin/groovy

String call() {
    sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
}