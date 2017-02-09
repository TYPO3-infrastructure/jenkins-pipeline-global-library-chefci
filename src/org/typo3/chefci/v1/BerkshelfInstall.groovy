#!/usr/bin/env groovy

package org.typo3.chefci.v1;

import groovy.json.JsonSlurper
import org.apache.commons.lang.RandomStringUtils

def berksInstall(){
    sh 'berks install'
}

def subscribeToUpstreamJobs() {

    // list of dependent cookbooks and version constraints
    Map depsWithConstraint = getCookbookDependencies()
    ArrayList deps = depsWithConstraint.keySet()
    println "Cookbook dependencies: ${deps}"

    // stores the list of jobs for cookbooks that are a dependency of this cookbook
    def existingUpstreamCookbookJobs = []

    def thisCookbookName = getMetadata("name")

    // Jenkins CPS wtf
    for (i = 0; i < deps.size(); i++) {
        String cookbookName = deps[i]

        // "berks list" includes the cookbook itself, skip this
        if (cookbookName.equals(thisCookbookName)) {
            continue
        }

        // create the upstream job name, i.e. GithubOrg/cookbookName/branchName
        def upstreamJobName = getUpstreamJobName(cookbookName)

        if (jobExists(upstreamJobName)) {
            // echo "Adding upstream job ${upstreamJobName}"
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
Map parseJson(String txt){
    // JsonSlurper returns non-serializable LazyMap. Convert it to HashMap
    // see http://stackoverflow.com/a/38899227/400222
    final slurper = new groovy.json.JsonSlurper()
    return new HashMap<>(slurper.parseText(txt))
}

Map parseJsonCommand(String command){
    return parseJson(sh(script: command, returnStdout: true))
}

@NonCPS
String createTempLocation(String path) {
    String tmpDir = pwd(tmp: true)
    return tmpDir + File.separator + new File(path).getName()
}

// returns the path to a temp location of a script from the global library (resources/ subdirectory)
String globalLibraryScript(String path) {

    String tmpPath = createTempLocation(path)
    // writeFile does not overwrite, so we delete the file first
    deleteFile(tmpPath)
    writeFile(file: tmpPath, text: libraryResource(path))
    echo "globalLibraryScript: copied ${path} to ${tmpPath}"
    return tmpPath
}

def deleteFile(String path) {
    if (fileExists(path)) {
        // echo "File ${path} already exists. Deleting"
        new File(path).delete()
    } else {
        // echo "File ${path} does not exist."
    }
}
// returns the dependencies of the cookbook as JSON ({"dependency" => "constraint", ...})
Map getCookbookDependencies() {
    String dependenciesAsJson = getMetadata("dependencies")
    Map data = parseJson(dependenciesAsJson)
    // println "getCookbookDependencies: dependencies are ${data}"
    return data
}

// calls the resources/cookbook/metadata.rb helper with the "type" parameter to read cookbook metadata
String getMetadata(String type) {
    String metadataScript = globalLibraryScript("cookbook/metadata.rb")
    def command = ['/opt/chefdk/embedded/bin/ruby', metadataScript, type].join(' ')
    println "Running metadata command ${command}"
    String result = sh(script: command, returnStdout: true)
    println "Read metadata ${type}: ${result}"
    return result
}


@NonCPS
String getUpstreamJobName(String cookbook) {

    // the name of the organization folder
    def folderName = currentBuild.rawBuild.getParent().getParent().getParent().getName()

    def jobName = "${folderName}/${cookbook}/master"
    // echo "Resulting job name: ${jobName}"
    return jobName
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
        // this.subscribeToUpstreamJobs()
    }
}

return this;
