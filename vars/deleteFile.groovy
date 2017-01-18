#!/usr/bin/groovy

@NonCPS
def call(String path) {
    if (fileExists(path)) {
        // echo "File ${path} already exists. Deleting"
        new File(path).delete()
    } else {
        // echo "File ${path} does not exist."
    }
}