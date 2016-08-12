#!/usr/bin/env groovy
package org.typo3.chefci.v1

def createKitchenYaml(){
    echo "createKitchenYaml()"

    if (fileExists('.kitchen.docker.yml')) {
        echo('Using the cookbooks .kitchen.docker.yml')
    } else {
        echo('Placing default .kitchen.docker.yml file in workspace')
        writeFile(
                file: '.kitchen.docker.yml',
                text: '''
driver:
  name: docker
  use_sudo: false
  provision_command:
    - apt-get install -y wget # this is only needed as long as Debian 8 does not trust bintray.com
    - apt-get install -y net-tools cron
    ''')
    }
}

def setKitchenLocalEnv(){
    env.KITCHEN_LOCAL_YAML=".kitchen.docker.yml"
}

def ArrayList<String> getInstances(){
    def tkInstanceNames = []

    node {
        // read out the list of test instances from `kitchen list`
        def lines = sh(script: 'kitchen list', returnStdout: true).split('\n')
        // skip the headline, read out all instances
        for (int i = 1; i < lines.size(); i++) {
            tkInstanceNames << lines[i].tokenize(' ')[0]
        }
    }
    return tkInstanceNames
}

def parallelConverge() {
    this.parallelConverge(this.getInstances())
}

def parallelConverge(ArrayList<String> instanceNames) {
    def parallelNodes = [:]

    for (int i = 0; i < instanceNames.size(); i++) {
        def instanceName = instanceNames.get(i)
        parallelNodes["tk-${instanceName}"] = this.getNodeForInstance(instanceName)
    }

    parallel parallelNodes
}

def Closure getNodeForInstance(String instanceName) {
    return {
        // this node (one per instance) is later executed in parallel
        node {
            // restore workspace
            unstash('cookbook-tk')

            wrap([$class: 'AnsiColorBuildWrapper', colorMapName: "XTerm"]) {
                sh('kitchen test --destroy always ' + instanceName)
            }
        }
    }
}

def prepare(){
    stage "test-kitchen"

    node {
        this.createKitchenYaml()
        this.setKitchenLocalEnv()
        stash("cookbook-tk")
    }
}

def execute(){
    this.prepare()
    // this will allocate multiple nodes
    this.parallelConverge()
}

return this;