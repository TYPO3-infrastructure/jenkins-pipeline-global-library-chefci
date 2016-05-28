#!/usr/bin/env groovy

package org.typo3.chefci.v1;

def berksInstall(){
    try {
        sh('berks install')
    } catch (err) {
        failTheBuild {
            message = "'berks install' failed"
        }
    }
}
def execute(){
    stage('resolve dependencies')
    node {
        this.berksInstall()
    }
}

return this;
