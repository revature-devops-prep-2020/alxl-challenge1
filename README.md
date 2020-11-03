# Challenge 1
Build a Jenkins CI/CD pipeline for a Dockerized Spring/Node/DotNet microservice on a Kubernetes environment on AWS using Jenkins. Set up your cloud infrastructure using `awscli`, register a GitHub hook to trigger a Jenkins build, and validate code quality with SonarCloud before publishing a Docker image to DockerHub. Use `kubectl` to deploy the image to both a testing and staging environment. Report build, test, and deployment results via Slack/Discord or email message.

# Setup
## Configure AWS CLI
Use `aws configure` to supply the Access Key ID and the Secret Access Key for your IAM role.

## Create EKS Cluster
### With eksctl
Amazon recommends using [eksctl](https://eksctl.io/) to create EKS clusters. Use the following commands to install eksctl:
```sh
# Install on Windows

chocolatey install eksctl

# Install on Linux

curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp

sudo mv /tmp/eksctl /usr/local/bin
```

The command to create a cluster with eksctl takes several arguments; they are all optional and will be filled with [default values](https://github.com/weaveworks/eksctl#basic-usage) if unspecified.
```sh
eksctl create cluster \
    --name <autogen-cluster-name> \
    --version <1.17> \
    --region <us-west-2> \
    --nodegroup-name <autogen-nodegroup-name> \
    --nodes <3> \
```
This process can take several minutes.

## Spin up Jenkins
### Build and Push Docker Image
The Dockerfile for the Jenkins server is specified in the root directory. If you intend to use your own image, be sure to change `kube/deployment.yml` to pull your image.
```sh
docker build -t user/revcog-jenkins:latest jenkins/
docker push user/revcog-jenkins:latest
```

### Kubernetes Deployment and Service
With your kubectl context set up, run the following command to start the Jenkins deployment and service:
```sh
kubectl apply -f kube/
```
This can take a few minutes. Once it is finished, the following command will give you the external IP of your Jenkins server:
```sh
kubectl get svc -n revcog
```
Navigating to that IP in your browser will give you access to the Jenkins server.