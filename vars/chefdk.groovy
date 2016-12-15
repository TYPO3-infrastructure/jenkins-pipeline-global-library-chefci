#!/usr/bin/env groovy

def call(body) {
    withDockerContainer(image: 'chef/chefdk:latest', args: '--volume=/var/lib/jenkins/.chef/:/.chef/:ro --volume=/var/lib/jenkins/.berkshelf/:/.berkshelf/:rw') {
        body()
    }
}
