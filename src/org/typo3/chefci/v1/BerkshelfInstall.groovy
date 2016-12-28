#!/usr/bin/env groovy

package org.typo3.chefci.v1;

import groovy.json.JsonSlurper

def berksInstall(){
    sh 'berks install'
}

def subscribeToUpstreamJobs() {
    def output = sh(script: "berks list -F json", returnStdout: true)
    def data = parseJson(output)

    // list of cookbook names
    def ArrayList deps = getCookbookDependencies(data)

    // stores the list of jobs for cookbooks that are a dependency of this cookbook
    def existingUpstreamCookbookJobs = []

    // Jenkins CPS wtf
    for (i = 0; i < deps.size(); i++) {
        def cookbookName = deps[i]

        // "berks list" includes the cookbook itself, skip this
        if (cookbookName.equals(getCookbookName())) {
            continue
        }

        // create the upstream job name, i.e. GithubOrg/cookbookName/branchName
        def upstreamJobName = getUpstreamJobName(cookbookName)

        if (jobExists(upstreamJobName)) {
            // echo "Adding upstream job #${upstreamJobName}"
            existingUpstreamCookbookJobs << upstreamJobName
        } else {
            // echo "Skipping dependency ${cookbookName}"
        }
    }

    echo "Upstream jobs: ${existingUpstreamCookbookJobs}"

    // set job properties to subscribe to upstream jobs
    properties([
            pipelineTriggers([
                    upstream(
                            threshold: hudson.model.Result.SUCCESS,
                            upstreamProjects: existingUpstreamCookbookJobs.join(', ')
                    )
            ])
    ])
}

@NonCPS
def parseJson(txt){
    return new groovy.json.JsonSlurper().parseText(txt)
}

@NonCPS
def getCookbookDependencies(data) {
    // extracts interesting data from `berks list -F json` output.
    // reads the "cookbooks" entry of the Map and returns the "name" key of each
    // result is an ArrayList of cookbook names
    return data.cookbooks.collect { it -> it.name }
}

@NonCPS
def getUpstreamJobName(String cookbook) {

    // the name of the organization folder
    def folderName = currentBuild.rawBuild.getParent().getParent().getParent().getName()

    def jobName = "${folderName}/${cookbook}/master"
    // echo "Resulting job name: ${jobName}"
    return jobName
}

@NonCPS
def getCookbookName() {
    return currentBuild.rawBuild.getParent().getParent().getName()
}

@NonCPS
def jobExists(String jobName) {
    // checks if a job exists
    def exists = (Jenkins.instance.getItemByFullName(jobName) != null)
    // echo exists ? "Job ${jobName} exists" : "Job ${jobName} does NOT exist"
    return exists
}


def execute(){
    stage('resolve dependencies')
    node {
        this.berksInstall()
        this.subscribeToUpstreamJobs()
    }
}

return this;
