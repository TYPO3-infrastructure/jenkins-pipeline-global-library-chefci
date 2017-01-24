package org.typo3.chefci.v2.stages

class Acceptance extends AbstractStage {

    /*
     Name of the file that is placed inside the cookbook folder.
     Can be changed using setKitchenLocalYmlName('.kitchen.docker.yml')
      */
    def kitchenLocalYmlName = '.kitchen.local.yml'

    Acceptance(Object script, String stageName) {
        super(script, stageName)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            testkitchen()
        }
    }

    void setKitchenLocalYmlName(String filename) {
        kitchenLocalYmlName = filename
        this
    }

    private testkitchen() {
        script.node {

            createKitchenYaml()

            script.wrap([$class: 'AnsiColorBuildWrapper', colorMapName: "XTerm"]) {
                script.withEnv(["KITCHEN_LOCAL_YAML=${kitchenLocalYmlName}"]) {
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

    /**
     * Checks if a local kitchen config file (defaults to .kitchen.local.yml) already exists
     * and places the one from this library otherwise (resources/cookbook/ folder).
     */
    private createKitchenYaml() {

        if (! kitchenLocalYmlName) {
            script.echo "No local kitchen config file specified. Doing nothing."
        } else if (script.fileExists(kitchenLocalYmlName)) {
            script.echo "Using the cookbook's ${kitchenLocalYmlName}"
        } else {
            script.echo "Placing default ${kitchenLocalYmlName} file in workspace"
            jenkinsHelper.copyGlobalLibraryScript "cookbook/${kitchenLocalYmlName}", kitchenLocalYmlName
        }
    }

}
