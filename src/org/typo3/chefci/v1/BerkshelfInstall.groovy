#!/usr/bin/env groovy

package org.typo3.chefci.v1;

import groovy.json.JsonSlurper

def berksInstall(){
    sh 'berks install'
}

def subscribeToUpstreamJobs() {
    def output = sh(script: "berks list -F json", returnStdout: true)
    def data = parseJson(output)

    def deps = getCookbookDependencies(data)
    def existingUpstreamCookbookJobs = []
    for (i = 0; i < deps.size(); i++) {
        def cookbookName = deps[i]

        // echo "currentBuild: ${currentBuild.getName()}"
//        if (cookbookName.equals(currentBuild.getName())) {
//            echo "Skipping myself"
//            continue
//        }

        def upstreamJobName = getUpstreamJobName(cookbookName)
        echo "#${i}: ${cookbookName} -> ${upstreamJobName}"

        if (jobExists(upstreamJobName)) {
            echo "Adding upstream job #${upstreamJobName}"
            existingUpstreamCookbookJobs << upstreamJobName
        } else {
            echo "Skipping dependency ${cookbookName}"
        }
    }

    echo "Existing dependencies: ${existingUpstreamCookbookJobs}"

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
    return data.cookbooks.collect { it -> it.name }
}

//def setUpstreamJobs(cookbooks) {
//    def existingJobs = filterExistingUpstreamJobs(cookbooks)
//    echo "Existing upstream jobs: ${existingJobs}"
//    // def upstreamList = existingJobs.join(', ')
//    echo "Existing upstream job list: ${upstreamList}"
//    properties([
//            pipelineTriggers([
//                    upstream(
//                            // threshold: hudson.model.Result.SUCCESS,
//                            upstreamProjects: upstreamList
//                    )
//            ])
//    ])
//
//    // cookbooks.each { it }
//}

def getUpstreamJobName(String cookbook) {
    def jobName = "TYPO3-cookbooks/${cookbook}/develop"

    echo "Parent: ${currentBuild.rawBuild.getParent().getParent().getParent()}"
    // echo "Resulting job name: ${jobName}"
    return jobName
}

def jobExists(String jobName) {
    def exists = (Jenkins.instance.getItemByFullName(jobName) != null)
    echo exists ? "Job ${jobName} exists" : "Job ${jobName} does NOT exist"
    return exists
}

//@NonCPS
//def filterExistingUpstreamJobs(ArrayList cookbooks) {
//    println "Filtering existing ones out of ${cookbooks}"
//    //def existingCookbooks = cookbooks.collect{ name ->
//    //    if (jobExists(name)) { name }
//    //}
//    //println "Existing cookbooks: ${existingCookbooks}"
//    //return existingCookbooks.findAll()
//    def existingUpstreamJobs = [:]
//    for (cb in cookbooks) {
//        if (cookbookJobExists(cb)) {
//            existingUpstreamJobs << cb
//        }
//    }
//    // def upstreamJobs = cookbooks.findAll { cookbook -> cookbookJobExists(cookbook) }
//    println "Remaining upstream jobs: ${existingUpstreamJobs}"
//    return existingUpstreamJobs
//}

def execute(){
    stage('resolve dependencies')
    node {
        this.berksInstall()
        this.subscribeToUpstreamJobs()
    }
}

return this;
