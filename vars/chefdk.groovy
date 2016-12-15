#!/usr/bin/env groovy

def call(body) {
    withDockerContainer(image: 'chef/chefdk:latest', args: '--volume=/var/lib/jenkins/.chef/:/.chef/:ro') {
        body()
    }
}
