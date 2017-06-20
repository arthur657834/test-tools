https://jenkins.io/doc/book/pipeline/

如果只有一个强制的参数，则可以省略参数名字，如下两种等价效果：
```
sh 'echo hello'
sh([script: 'echo hello'])
```
```
选中两台slave
node('unix && 64bit')

尽量使用stash来实现stage/node间共享文件，不要使用archive

在stash被引入pipeline DSL前，一般使用archive来实现node或stage间文件的共享。 在stash引入后，最好使用stash/unstash来实现node/stage间文件的共享。例如在不同的node/stage间共享源代码。

archive用来实现更长时间的文件存储。

stash excludes: 'target/', name: 'source'
unstash 'source'
archive ‘target/*.jar'

函数定义
def version() {
def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
matcher ? matcher[0][1] : null
}

```

ex1:
```
node{
  stage('get clone')
  {
  git url: 'https://github.com/jglick/simple-maven-project-with-tests.git'
  //git url: 'https://github.com/jglick/simple-maven-project-with-tests.git', branch: 'master'
  //check CODE
  }
  stage('mvn build')
  {
  //mvn构建
  timeout(time: 30, unit: 'SECONDS')
   {
     //设置30秒超时时间
  bat 'mvn install -Dmaven.test.skip=true'
   }
  }
  stage('deploy')
  //stage name:'deploy', concurrency: 1
   {
  bat 'deploy.bat'
     //执行部署脚本
   }
}
```

build job: 'test-Pipline', parameters: [string(name: 'Stringinput', value: '1'), booleanParam(name: 'buildsucess', value: true)]

ex2:
```
node('master')
{
    stage('check system')
    {
        if(isUnix()){
            echo "this is Unix"
        }
        else
        {
            echo "this is Linux"
        }
    }
}
node('ljtest-186') {
    bat 'pybot hello_world'
}
```
ex3:
```
parallel 'integration-tests':{
    node('master'){
    echo "master"
    }
}, 'functional-tests':{
    node('ljtest-186'){
    echo "ljtest-186"
    }
}
```
ex4:
```
input 能够暂停pipeline的执行等待用户的approve（自动化或手动），通常地approve需要一些时间等待用户相应。 如果在node里使用input将使得node本身和workspace被lock， 不能够被别的job使用。

stage 'deployment'
input 'Do you approve deployment?'
node{
    //deploy the things
}

pipeline可以很容易地使用timeout来对step设定timeout时间。对于input我们也最好使用timeout。

timeout(time:5, unit:'DAYS') {
    input message:'Approve deployment?', submitter: 'it-ops'
}
```
ex5:
```
不建议使用env来修改全局的环境变量，这样后面的groovy脚本也将被影响。

一般使用withEnv来修改环境变量，变量的修改只在withEnv的块内起作用。

从Global Tool Configuration中取值
def mvnHome = tool 'M3'

env.PATH = "${mvnHome}/bin:${env.PATH}"

node{
    echo "$PATH"
    echo "----------------------"
    withEnv(["PATH+MAVEN=${tool 'm3'}/bin"])
    {
        echo "$PATH"
    }
    echo "$PATH"
}
```

ex6:
```
记录测试结果和构建产物

当有测试用例失败的时候，你可能需要保存失败的用例结果和构建产物用于人工排查错误。

node {
git url: 'https://github.com/jglick/simple-maven-project-with-tests.git'
def mvnHome = tool 'M3'
sh "${mvnHome}/bin/mvn -B -Dmaven.test.failure.ignore verify"
step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
}
上面的例子中，-Dmaven.test.failure.ignore将忽略测试用例的失败，jenkins将正常执行后面的step然后退出。
上面的两个step调用相当于调用传统的jenkins的steps将构建产物和测试结构保存。
```

ex7:
```
本地变量的序列化

如下的代码在运行时可能会遇到错误java.io.NotSerializableException: java.util.regex.Matcher，错误的原因是Matcher是不可序列化的类型。

node('remote') {
git url: 'https://github.com/jglick/simple-maven-project-with-tests.git'
def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
if (matcher) {
echo "Building version ${matcher[0][1]}"
}
def mvnHome = tool 'M3'
sh "${mvnHome}/bin/mvn -B -Dmaven.test.failure.ignore verify"
step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
}

pipeline job为了支持能够在jenkins重启后恢复继续运行，jenkins在后台定期地将job的运行状态保存到硬盘。保存的动作一般在每个step结束后，或者在一些step的中间，例如sh step的中间。
jenkins保存的job的状态，包括整个控制流程，例如局部变量，循环所在的位置，等等。正因为如此，groovy里的任何变量必须是number，string或可序列化的类型，其他的例如网络连接等是不能够序列化的。
如果你临时地使用不可序列化的类型，则需要在使用完马上释放。如果局部变量在函数中，函数调用结束的时候局部变量也会被自动释放。我们也可以显示地释放局部变量。 如下

node('remote') {
git url: 'https://github.com/jglick/simple-maven-project-with-tests.git'
def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
if (matcher) {
echo "Building version ${matcher[0][1]}"
}
matcher = null
def mvnHome = tool 'M3'
sh "${mvnHome}/bin/mvn -B -Dmaven.test.failure.ignore verify"
step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
}

然而最安全的方法是将不可序列化的语句隔离到函数中，且在函数的前面增加属性@NonCPS。通过这种方法pipeline将识别此函数为native且不保存对应的局部变量。另外使用了@NoCPS的函数中不能够调用其他的pipeline steps，例如必须将readFile放到函数外面：
node('remote') {
git url: 'https://github.com/jglick/simple-maven-project-with-tests.git'
def v = version(readFile('pom.xml'))
if (v) {
echo "Building version ${v}"
}
def mvnHome = tool 'M3'
sh "${mvnHome}/bin/mvn -B -Dmaven.test.failure.ignore verify"
step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
}
@NonCPS
def version(text) {
def matcher = text =~ '<version>(.+)</version>'
matcher ? matcher[0][1] : null
}

上面的加了@NoCPS的version函数将被正常的groovy运行时执行，所以任何的局部变量都是允许的。
```

ex8:
```
创建多线程

pipeline能够使用parallel来同时执行多个任务。 parallel的调用需要传入map类型作为参数，map的key为名字，value为要执行的groovy脚本。
为了测试parallel的运行，可以安装parallel test executor插件。此插件可以将运行缓慢的测试分割splitTests。

用下面的脚本新建pipeline job：

node('remote') {
git url: 'https://github.com/jenkinsci/parallel-test-executor-plugin-sample.git'
archive 'pom.xml, src/'
}
def splits = splitTests([$class: 'CountDrivenParallelism', size: 2])
def branches = [:]
for (int i = 0; i < splits.size(); i++) {
def exclusions = splits.get(i);
branches["split${i}"] = {
node('remote') {
sh 'rm -rf *'
unarchive mapping: ['pom.xml' : '.', 'src/' : '.']
writeFile file: 'exclusions.txt', text: exclusions.join("\n")
sh "${tool 'M3'}/bin/mvn -B -Dmaven.test.failure.ignore test"
step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/*.xml'])
}
}
}
parallel branches



如果遇到RejectedAccessException错误，需要管理员approve权限staticMethod org.codehaus.groovy.runtime.ScriptBytecodeAdapter compareLessThan java.lang.Object java.lang.Object。

当第一次运行上面的pipeline job的时候，所有的测试顺序执行。当第二次或以后执行的时候，splitTests将会将所有的测试分割为大概等价的两份，然后两个task并行运行。如果两个task运行在不同的slave上，则可以看到job总的时间将会减半。

下面的等价语句用来打包pom.xml和源代码：
archive 'pom.xml, src/'
step([$class: 'ArtifactArchiver', artifacts: 'pom.xml, src/'])

我们可以看到prallel里的语句使用了node，这意味着并行执行的任务将会在新的node/slave上执行，且使用不同的workspace，为了确保所有的node和workspace使用相同的代码，所以才有了前面的打包archive和parallel里的解包unarchive。

上面的例子中我们可以看到同一个pipeline job里可以使用多个node，多个node会有不同的workspace，我们需要确保每个workspace的内容都是我们想要的内容。

另一个问题，如果在pipeline中使用env，环境变量的修改会在整个pipeline起作用，如果只修改parallel并行的线程的变量，可以使用withEnv。

在使用了parallel的console log里，并行的log都混在了一起，需要在job的pipeline steps页面查看按逻辑分割的更情况的log。
```
