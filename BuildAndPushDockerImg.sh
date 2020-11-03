if [ -z $1 ]; then
    echo "Enter Docker Hub username"
    read username
else
    username=$1
fi
if [ -z $2 ]; then
    tag="latest"
else
    tag=$2
fi
docker build -t $username/revcog-jenkins:$tag $(dirname $0)
docker push $username/revcog-jenkins:$tag