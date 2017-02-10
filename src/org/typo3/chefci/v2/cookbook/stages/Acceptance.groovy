package org.typo3.chefci.v2.cookbook.stages

import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack
import org.typo3.chefci.v2.shared.stages.AbstractStage

class Acceptance extends AbstractStage {
    /**
     Name of the file that is placed inside the cookbook folder.
     Can be changed using setKitchenLocalYml('.kitchen.docker.yml')
     */
    def kitchenLocalYmlName = '.kitchen.local.yml'

    /**
     * Name of the stashed cookbook contents. Does not really matter.
     */
    def stashName = 'cookbook'


    Acceptance(Object script, JenkinsHelper jenkinsHelper, Slack slack) {
        super(script, 'Acceptance', jenkinsHelper, slack)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            testKitchen()
        }
    }

    protected testKitchen() {
        script.node {
            createKitchenYaml()
            script.stash stashName
        }

        script.parallel getInstancesForParallel()
    }

    /**
     * Checks if a local kitchen config file (defaults to .kitchen.local.yml) already exists
     * and places the one from this library otherwise (resources/cookbook/ folder).
     */
    protected createKitchenYaml() {

        if (!kitchenLocalYmlName) {
            script.echo "No local kitchen config file specified. Doing nothing."
        } else if (script.fileExists(kitchenLocalYmlName)) {
            script.echo "Using the cookbook's ${kitchenLocalYmlName}"
        } else {
            script.echo "Placing default ${kitchenLocalYmlName} file in workspace"
            jenkinsHelper.copyGlobalLibraryScript "cookbook/${kitchenLocalYmlName}", kitchenLocalYmlName
        }
    }

    /**
     * Does a lot of magic to return a Map containing entires for every test-kitchen instance.
     * Result of this should be fed into the "parallel" step.
     *
     * @return Map in format [instance1: { node { ... }}, instance2: { node {..}}]
     */
    protected getInstancesForParallel() {
        def parallelNodes = [:]
        def instanceNames = getInstanceNames()

        for (int i = 0; i < instanceNames.size(); i++) {
            def instanceName = instanceNames.get(i)
            parallelNodes[instanceName] = getNodeClosureForInstance(instanceName)
        }
        parallelNodes
    }

    /**
     * Runs the `kitchen list` command and returns a list of all instance names.
     *
     * @return list of instance names
     */
    protected ArrayList<String> getInstanceNames() {
        def tkInstanceNames = []
        List<String> lines

        script.node {
            script.unstash stashName

            script.withEnv(["KITCHEN_LOCAL_YAML=${kitchenLocalYmlName}"]) {
                // read out the list of test instances from `kitchen list`
                lines = script.sh(script: 'kitchen list', returnStdout: true).split('\n')
            }
        }

        // skip the headline, read out all instances
        for (int i = 1; i < lines.size(); i++) {
            tkInstanceNames << lines[i].tokenize(' ').first()
        }
        tkInstanceNames
    }

    /**
     * Retruns a closure containing the node step with all the stuff to be done for the specified test-kitchen instance
     *
     * @param instanceName name of the test-kitchen instance (like default-debian-8)
     * @return Closure that is later feeded into the parallel step
     */
    protected Closure getNodeClosureForInstance(String instanceName) {
        return {
            // this node (one per instance) is later executed in parallel
            script.node {
                // restore workspace
                script.unstash stashName

                script.wrap([$class: 'AnsiColorBuildWrapper', colorMapName: "XTerm"]) {
                    script.withEnv(["KITCHEN_LOCAL_YAML=${kitchenLocalYmlName}"]) {
                        try {
                            script.sh script: "kitchen test --destroy always ${instanceName}"
                        } catch (err) {
                            script.echo "Archiving test-kitchen logs due to failure condition"
                            script.archive includes: ".kitchen/logs/${instanceName}.log"
                            script.error "test-kitchen failed for instance ${instanceName}"
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the file name of the overriding kitchen yml file, e.g. .kitchen.docker.yml instead of .kitchen.local.yml
     * @param filename
     */
    void setKitchenLocalYml(String filename) {
        kitchenLocalYmlName = filename
        this
    }

}
