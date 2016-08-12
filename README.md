# Jenkins Global Library for TYPO3 Chef CI

This repository contains common functionality used by [our Jenkins server](https://chef-ci.typo3.org) for testing [our Chef cookbooks](https://github.com/TYPO3-cookbooks).

The pipeline defined [here](https://github.com/TYPO3-infrastructure/jenkins-pipeline-global-library-chefci/blob/master/src/org/typo3/chefci/v1/Pipeline.groovy) is used by a `Jenkinsfile` in each of these repositories:

```groovy
// Jenkinsfile
def pipe = new org.typo3.chefci.v1.Pipeline()
pipe.execute()
```

## More Information

- [Jenkins Pipeline plugin suite](https://jenkins.io/pipeline/getting-started-pipelines/)
- [Jenkins Pipeline Global Library](https://github.com/jenkinsci/workflow-cps-global-lib-plugin)
- [Our Chef cookbooks](https://github.com/TYPO3-cookbooks)
- [Our Jenkins server for Chef coobkook testing](https://chef-ci.typo3.org)
