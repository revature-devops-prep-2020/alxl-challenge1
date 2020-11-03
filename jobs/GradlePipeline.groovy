pipeline {
    agent any

    tools {
        gradle 'gradle'
    }

    stages {
        stage('Pull') {
            steps {
                git url: "${gitURL}", branch: "${gitBranch}"
            }
        }

        stage('build') {
            steps {
                sh 'chmod +x gradlew && ./gradlew build'
            }
            post {
                failure {
                    slackSend(color: 'danger', channel: "${slackChannel}",
                    message: "project '${projectName}' [${gitBranch}:${currentBuild.number}] has failed to build.")
                }
            }
        }

        stage('sonarqube') {
            steps {
                withSonarQubeEnv('sonar-server') {
                    sh './gradlew sonarqube'
                }
            }
            post {
                failure {
                    slackSend(color: 'danger', channel: "${slackChannel}",
                    message: "project '${projectName}' [${gitBranch}:${currentBuild.number}] has failed to get scanned by SonarCloud.")
                }
            }
        }

        stage('Make Docker image') {
            steps {
                sh "docker build -t alxl/${projectName}:${currentBuild.number} ."
                sh "docker tag alxl/${projectName}:${currentBuild.number} alxl/${projectName}:latest"
            }
            post {
                failure {
                    slackSend(color: 'danger', channel: "${slackChannel}",
                    message: "project '${projectName}' [${gitBranch}:${currentBuild.number}] has failed to Dockerize.")
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withDockerRegistry([credentialsId: 'dockerhub-creds', url: '']) {
                    sh "docker push alxl/${projectName}:${currentBuild.number}"
                    sh "docker push alxl/${projectName}:latest"
                }
            }
            post {
                failure {
                    slackSend(color: 'danger', channel: "${slackChannel}",
                    message: "project '${projectName}' [${gitBranch}:${currentBuild.number}] has failed to push to the CR.")
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withKubeConfig([credentialsId: 'kubectl-creds', serverUrl: "${kubectlServer}"]) {
                    sh 'kubectl apply -f kube/ -n revcog-test'
                    sh 'kubectl apply -f kube/ -n revcog-prod'
                    sh "kubectl rollout restart deployment/${projectName} -n revcog-test"
                    sh "kubectl rollout restart deployment/${projectName} -n revcog-prod"
                }
            }
            post {
                failure {
                    slackSend(color: 'danger', channel: "${slackChannel}",
                    message: "project '${projectName}' [${gitBranch}:${currentBuild.number}] has failed to deploy to K8s.")
                }
            }
        }
    }
    post {
        success {
            slackSend(color: 'good', channel: "${slackChannel}",
                message: "project '${projectName}' [${gitBranch}:${currentBuild.number}] has passed all tests and was successfully built and pushed to the CR.")
        }
    }
}

