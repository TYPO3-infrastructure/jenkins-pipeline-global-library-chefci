#!/usr/bin/env groovy

package org.typo3.chefci.v1;

def archiveArtifacts(){
    artifactName = "cookbooks-${env.BUILD_TAG}.tar.gz"
    sh("berks package ${artifactName}")
    archive(includes: artifactName)
}

def execute(){
    stage('archive artifacts')
    node {
        archiveArtifacts()
    }
}

return this;
