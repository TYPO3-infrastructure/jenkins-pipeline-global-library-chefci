#!/usr/bin/groovy

String call(String sha = 'HEAD') {
    sh(returnStdout: true, script: 'git rev-parse ' + sha).trim()
}