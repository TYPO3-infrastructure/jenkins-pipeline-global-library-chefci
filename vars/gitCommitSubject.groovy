#!/usr/bin/groovy

String call(String sha = 'HEAD') {
    sh(returnStdout: true, script: 'git show -s --format=%s ' + sha).trim()
}