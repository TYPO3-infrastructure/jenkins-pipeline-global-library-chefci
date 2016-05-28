#!/usr/bin/env groovy

package org.typo3.chefci.v1;

def berksUpload(){
    sh("berks upload")
}

def execute(){
    stage('upload')
    node {
        this.berksUpload()
    }
}

return this;