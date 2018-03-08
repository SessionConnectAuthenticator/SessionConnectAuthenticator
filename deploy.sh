set -e

# kubernetes config url
export KUBECONFIG=/home/travis/.bluemix/plugins/container-service/clusters/mbi/kube-config-par01-mbi.yml

#check if the container needs to be deployed, and if not exit the script
if [ "$DEPLOY" = "false" ]; then 
  printf "\n\nskipping deployment"
  exit 0
else

# install bluemix and the required plugins
curl https://clis.ng.bluemix.net/download/bluemix-cli/0.6.2/linux64 -o Bluemix_CLI.tar.gz -L| sh
tar -xvf Bluemix_CLI.tar.gz
cd Bluemix_CLI
sudo ./install_bluemix_cli
cd ..
yes|bx update
bx plugin install container-service -r Bluemix
bx plugin install container-registry -r Bluemix

# install kubernetes 
curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl
chmod +x ./kubectl
sudo mv ./kubectl /usr/local/bin/kubectl

# deploy
# login and configure bluemix cli
bx api https://api.eu-de.bluemix.net
bx login --apikey $API_KEY
bx cr login
bx target -r eu-de -o tud_mbi -s dev
#push and deploy images
bx cs cluster-config mbi
if [[ $(bx cr image-list -q) ]]; then
	bx cr image-rm $(bx cr image-list -q)
fi
kubectl delete deployment,service $DEPLOYMENT_ID $SERVICE_ID --ignore-not-found=true
docker push $IMAGE_NAME
kubectl apply -f deployment.yml
fi
