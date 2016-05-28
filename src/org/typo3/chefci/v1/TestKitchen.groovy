#!/usr/bin/env groovy
package org.typo3.chefci.v1

def createKitchenYaml(){
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






return this;