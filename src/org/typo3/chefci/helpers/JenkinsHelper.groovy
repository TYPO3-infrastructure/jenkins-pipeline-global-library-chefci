package org.typo3.chefci.helpers

class JenkinsHelper implements Serializable {
    def steps

    JenkinsHelper(steps) { this.steps = steps }

    @NonCPS
    String createTempLocation(String path) {
        String tmpDir = steps.pwd tmp: true
        return tmpDir + File.separator + new File(path).getName()
    }

    // returns the path to a temp location of a script from the global library (resources/ subdirectory)
    String copyGlobalLibraryScript(String srcPath, String destPath = null) {

        destPath = destPath ?: createTempLocation(srcPath)
        // writeFile does not overwrite, so we delete the file first
        steps.deleteFile destPath
        steps.writeFile file: destPath, text: steps.libraryResource(srcPath)
        steps.echo "copyGlobalLibraryScript: copied ${srcPath} to ${destPath}"
        return destPath
    }
}
