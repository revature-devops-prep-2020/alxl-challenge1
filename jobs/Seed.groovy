folder("${projectName}") {
    description("Jobs for project ${projectName}")
}

freeStyleJob("${projectName}/${gitBranch}-listen") {
    properties {
        githubProjectUrl("${gitURL}")
    }
    triggers {
        gitHubPushTrigger()
    }
    scm {
        git {
            remote {
                url("${gitURL}")
            }
            branches("*/${gitBranch}")
        }
    }
    publishers {
        downstream("${projectName}/${gitBranch}-pipe")
    }
}

pipelineJob("${projectName}/${gitBranch}-pipe") {
    parameters {
        stringParam('gitURL', "${gitURL}", 'URL for Github source code containing Jenkinsfile')
        stringParam('projectName', "${projectName}", 'Name of project')
        stringParam('gitBranch', "${gitBranch}", 'Branch to checkout')
        stringParam('slackChannel', "${slackChannel}", 'Slack channel for the msg')
        stringParam('kubectlServer', "${kubectlServer}", 'IP of Kubernetes API server')
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url("${gitURL}")
                    }
                    branch("*/${gitBranch}")
                }
            }
            lightweight()
        }
    }
}
