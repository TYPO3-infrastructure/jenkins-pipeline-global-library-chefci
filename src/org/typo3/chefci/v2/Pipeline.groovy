package org.typo3.chefci.v2

class Pipeline implements Serializable {

    Pipeline(def script) {
        this.script = script
    }

    def execute() {
        script.echo "Hallo Welt!"
    }

}

