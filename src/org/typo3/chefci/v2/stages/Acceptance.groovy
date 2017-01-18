package org.typo3.chefci.v2.stages

import org.typo3.chefci.helpers.JenkinsHelper

class Acceptance extends AbstractStage {

    Acceptance(Object script, String stageName) {
        super(script, stageName)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            testkitchen()
        }
    }

    private def testkitchen(){
        script.node {

            def jenkinsHelper = new JenkinsHelper(script)
            jenkinsHelper.copyGlobalLibraryScript 'cookbook/.kitchen.docker.yml', '.kitchen.docker.yml'

            script.wrap([$class: 'AnsiColorBuildWrapper', colorMapName: "XTerm"]) {
                script.withEnv(['KITCHEN_LOCAL_YAML=.kitchen.docker.yml']) {
                    try {
                        script.sh script: 'kitchen test --destroy always'
                    } catch (err) {
                        script.echo "Archiving test-kitchen logs due to failure condition"
                        // archive includes: ".kitchen/logs/${instanceName}.log"
                        script.error "kitchen returned non-zero exit status"
                    }
                }
            }
        }
    }

}
