#!/usr/bin/env groovy

package org.typo3.chefci.v1;

def berksInstall(){
    try {
        sh('berks install')
        currentBuild.result = 'SUCCESS'
    } catch (err) {
        currentBuild.result = 'FAILURE'
    }
}
def execute(){
    stage('resolve dependencies')
    node {
        this.berksInstall()
    }
}

return this;
