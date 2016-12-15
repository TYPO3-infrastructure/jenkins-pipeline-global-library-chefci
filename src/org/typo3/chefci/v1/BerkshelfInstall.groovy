#!/usr/bin/env groovy

package org.typo3.chefci.v1;

def berksInstall(){
    //chefdk {
    //    sh 'berks install'
    //}
    withDockerContainer(image: 'chef/chefdk:latest', args: '--volume=/var/lib/jenkins/.chef/:/.chef/:ro') {
        sh "berks install"
    }
}
def execute(){
    stage('resolve dependencies')
    node {
        this.berksInstall()
    }
}

return this;
