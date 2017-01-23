package org.typo3.chefci.helpers

class JenkinsHelper implements Serializable {
    def steps

    JenkinsHelper(steps) { this.steps = steps }

    /**
     * Generates a path to a temporary file location, ending with {@code path} parameter.
     *
     * @param path path suffix
     * @return path to file inside a temp directory
     */
    @NonCPS
    String createTempLocation(String path) {
        String tmpDir = steps.pwd tmp: true
        return tmpDir + File.separator + new File(path).getName()
    }

    /**
     * Returns the path to a temp location of a script from the global library (resources/ subdirectory)
     *
     * @param srcPath path within the resources/ subdirectory of this repo
     * @param destPath destination path (optional)
     * @return path to local file
     */
    String copyGlobalLibraryScript(String srcPath, String destPath = null) {

        destPath = destPath ?: createTempLocation(srcPath)
        // writeFile does not overwrite, so we delete the file first
        steps.deleteFile destPath
        steps.writeFile file: destPath, text: steps.libraryResource(srcPath)
        steps.echo "copyGlobalLibraryScript: copied ${srcPath} to ${destPath}"
        return destPath
    }

    /**
     * Annotates the build namme (#i by default) with some text, e.g., the version built.
     *
     * @param text Text to add to {@code currentBuild.displayName}
     */
    def annotateBuildName(String text) {
        steps.currentBuild.displayName = "#${steps.currentBuild.getNumber()} ${text}"
    }
}
