#!/usr/bin/env groovy

package org.typo3.chefci.v1;

def berksUpload(){
    echo "I could upload..."
}

def execute(){
    stage('upload')
    node {
        berksUpload()
    }
}

return this;