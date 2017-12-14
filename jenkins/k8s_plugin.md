```
kubectl get componentstatus 状态查询
kubectl cluster-info

kubectl get serviceAccounts
kubectl get namespace

kubectl get serviceaccount --all-namespaces
kubectl describe serviceaccount/default -n kube-system
kubectl get events

解决错误 No API token found for service account "default"
 vi /etc/kubernetes/apiserver
KUBE_ADMISSION_CONTROL="--admission-control=NamespaceLifecycle,NamespaceExists,LimitRanger,SecurityContextDeny,ResourceQuota"

  
kube UI 界面管理


插件：
cluster dns
cluster monitor ==> Heapster
cluster logging


cat > /tmp/serviceaccount.yaml <<EOF
apiVersion: v1
kind: ServiceAccount
metadata:
  name: build-robot
EOF
$ kubectl create -f /tmp/serviceaccount.yaml
serviceaccount "build-robot" created


kubectl describe pod centos7-jdk

yum install *rhsm* -y 

https://hub.docker.com/r/jenkinsci/jnlp-slave/
http://10.1.50.251:8080/jenkins/configureSecurity/ 开放TCP port for JNLP agents
添加节点，通过java-web启动，命名为ljtest，取得secret，然后运行
docker run jenkinsci/jnlp-slave -url http://10.1.50.251:8080/jenkins  4f538db41facefdb4b3fd10c0e57201b83a7cea8240a25e7995a787c5ced0556 ljtest


```
