#!/usr/bin/env groovy

package org.typo3.chefci.v1;

def foodcritic(){
    sh('foodcritic .')
}

def rubocop(){
    // see also http://atomic-penguin.github.io/blog/2014/04/29/stupid-jenkins-and-chef-tricks-part-1-rubocop/
    sh('rubocop --fail-level E')
    step([$class: 'WarningsPublisher', canComputeNew: false, canResolveRelativePaths: false, consoleParsers: [[parserName: 'Foodcritic'], [parserName: 'Rubocop']], defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', unHealthy: ''])
    step([$class: 'AnalysisPublisher'])
}


//////////////////////////

def execute(){
    stage('lint')
    node {
        this.foodcritic()
        this.rubocop()
    }
}

return this;
