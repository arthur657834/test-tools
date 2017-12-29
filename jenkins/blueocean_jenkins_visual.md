
https://jenkins.io/doc/book/blueocean/getting-started/
https://jenkins.io/doc/book/pipeline/syntax/

jenkins 安装 Blue Ocean插件


上传Jenkinsfile(pieline语法编写的)到git仓库

ex1:
```
pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Building..'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
    }
}

```
ex2:
Jenkinsfile (Declarative Pipeline)
```
pipeline {
    agent { docker 'python:3.5.1' }
    stages {
        stage('build') {
            steps {
                sh 'python --version'
            }
        }
    }
}
```

ex3: 
```groovy
  #!/usr/bin/env groovy  

  /*
   * Jenkinsfile for Jenkins2 Pipeline
  */  

  /* Only keep the 10 most recent builds. */
  def projectProperties = [
          [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '10']],
  ]  

  properties(projectProperties)  

  def access_token = "https://oapi.dingtalk.com/robot/send?access_token=xxxxx"  

  String branch = env.BRANCH_NAME  

  // 默认构建节点
  def node_label = 'master'  

  // 如果是Bug修复分支则在dev上面进行开发构建部署
  if (branch.startsWith('hotfix') || branch.startsWith('bugfix')) {
      node_label = 'dev'
  }  

  // Release分支在test节点构建
  if (branch.startsWith('R') || branch.startsWith('prod')) {
      node_label = 'test'
  }  

  // dev和test分支就在其对应的node上构建
  if (branch == 'dev' || branch == 'test') {
      node_label = branch
  }  
  

  node(node_label) {
      def mvnHome = tool 'maven3'
      env.PATH = "${mvnHome}/bin:${env.PATH}"  

      sh 'printenv'  

      try {
          stage('Clean workspace') {  

              /* Running on a fresh Docker instance makes this redundant, but just in
              * case the host isn't configured to give us a new Docker image for every
              * build, make sure we clean things before we do anything
              */
              deleteDir()
              sh 'ls -lah'
          }  
  

          stage('Checkout source') {
              checkout scm
              stash name: "code",
                      // excludes: "api/**,common/**",
                      includes: "**/**"
          }  

          // install to local
          stage('common') {  

              buildOrDeploy(branch, 'common/pom.xml')  

          }  

          stage('api') {
              buildOrDeploy(branch, 'api/pom.xml')
          }  

          // unit test
          timeout(time: 20, unit: 'MINUTES') {
              stage('test') {  

                  // 并发执行单元测试
                  // runTests()  

                  mvn '-f service/impl/pom.xml clean test -U'
                  junit 'service/impl/target/surefire-reports/TEST-*.xml'
              }
          }  

          // build bin
          stage('build') {
              buildOrDeploy(branch, 'pom.xml')  

          }  

          // 只在测试和开发分支进行自动部署
          if (isDeployBranch(branch)) {  

              // Omp部署
              deployOmp(branch)
          }  
  

          stage('Analyze') {  

              /*def scannerHome = tool 'SonarQube Scanner 3.0.3'
              def projectKey = "-Dsonar.projectKey=ljtest"
              def projectName = "-Dsonar.projectName=ljtest"
              def hostUrl = "-Dsonar.host.url=http://1.1.1.1:9000"
              def projectVersion = "-Dsonar.projectVersion=1.0.0"
              def sources = "-Dsonar.sources=service/impl/src/main/java," +
                      "web/impl/src/main/java," +
                      "gateway/impl/src/main/java," +
                      "common/util/src/main/java," +
                      "common/tenant/src/main/java"
              def SONAR_SCANNER_OPTS = hostUrl + " " + projectKey + " " + sources + " " + projectName + " " + projectVersion
              withEnv(["SONAR_SCANNER_OPTS=$SONAR_SCANNER_OPTS"]) {
                  sh "${scannerHome}/bin/sonar-scanner"
              }*/  

              // sh 'ls -lah'
              sh 'mvn org.jacoco:jacoco-maven-plugin:prepare-agent install -DskipTests -f service/impl/pom.xml -s ./settings.xml'
              sh 'mvn -f service/impl/pom.xml -s ./settings.xml sonar:sonar'  

          }  

          // 目前仅在dev分支生成文档
          if (branch == 'dev') {
              stage('doc') {
                  String webDoc = "web/swagger"
                  String webDocPath = "/var/www/arch/doc/api"  

                  sh "mvn -f ${webDoc}/pom.xml package"  

                  sh "mkdir -p ${webDocPath}"
                  sh "cp ${webDoc}/html/index.html ${webDocPath}"
                  sh "markdown_py ${webDoc}/changeLog.md -f ${webDocPath}/changelog.html"
              }
          }  

          /* 暂时取消归档，加快构建速度
          stage('Archive') {  

              archiveArtifacts artifacts: '**/ target/*.zip', fingerprint: true
          }
          */  

      } catch (Exception exc) {
          echo "${exc}"  

          if (!(exc instanceof groovy.lang.MissingPropertyException)) {  

              echo "发生异常，开始发送钉钉通知"
              def curl_body = "${access_token} -H 'Content-Type: application/json' -X POST -d '{\"msgtype\": \"text\", \"text\": { \"content\": \"JOB: ${JOB_NAME} \nSTATUS: FAIL \nException: ${exc} \nBUILD_ID: ${BUILD_DISPLAY_NAME} \nURL: ${BUILD_URL} \"}}'"
              sh "curl ${curl_body}"  

              echo "异常钉钉通知发送完成"
          }  

      }
  }  

  def runTests() {
      /* Request the test groupings.  Based on previous test results. */
      /* see https://wiki.jenkins-ci.org/display/JENKINS/Parallel+Test+Executor+Plugin and demo on github
      /* Using arbitrary parallelism of 4 and "generateInclusions" feature added in v1.8. */
      def splits = splitTests parallelism: [$class: 'CountDrivenParallelism', size: 2], generateInclusions: true  

      /* Create dictionary to hold set of parallel test executions. */
      def testGroups = [:]  

      for (int i = 0; i < splits.size(); i++) {
          def split = splits[i]  

          /* Loop over each record in splits to prepare the testGroups that we'll run in parallel. */
          /* Split records returned from splitTests contain { includes: boolean, list: List<String> }. */
          /*     includes = whether list specifies tests to include (true) or tests to exclude (false). */
          /*     list = list of tests for inclusion or exclusion. */
          /* The list of inclusions is constructed based on results gathered from */
          /* the previous successfully completed job. One additional record will exclude */
          /* all known tests to run any tests not seen during the previous run.  */
          testGroups["split-${i}"] = {  // example, "split3"
              node {
                  checkout scm  

                  /* Clean each test node to start. */
                  mvn 'clean'  

                  def mavenInstall = 'install -DMaven.test.failure.ignore=true'  

                  /* Write includesFile or excludesFile for tests.  Split record provided by splitTests. */
                  /* Tell Maven to read the appropriate file. */
                  if (split.includes) {
                      writeFile file: "target/parallel-test-includes-${i}.txt", text: split.list.join("\n")
                      mavenInstall += " -Dsurefire.includesFile=target/parallel-test-includes-${i}.txt"
                  } else {
                      writeFile file: "target/parallel-test-excludes-${i}.txt", text: split.list.join("\n")
                      mavenInstall += " -Dsurefire.excludesFile=target/parallel-test-excludes-${i}.txt"
                  }  

                  /* Call the Maven build with tests. */
                  mvn mavenInstall  

                  /* Archive the test results */
                  junit '**/target/surefire-reports/TEST-*.xml'
              }
          }
      }  
  

      parallel testGroups
  }  
  

  def mvn(def args) {
      sh "mvn ${args}"
  }  

  //========================== Tool Methods =========================
  /**
   * Maven deploy with skip Tests
   * @param pom pom file location for module
   * @return
   */
  def mvnDeploy(String pom) {
      mvnBuild(pom, "clean deploy")
  }  

  /**
   * Maven build with skip Tests
   * @param pom pom file location for module
   * @param command Maven phase
   * @return
   */
  def mvnBuild(String pom, String command = "package") {
      sh 'mvn -DskipTests -U -s ./settings.xml -f ' + pom + " " + command
  }  

  /**
   * only on master and deploy branchs perform deploy
   * @param branchName
   * @param pom
   * @return
   */
  def buildOrDeploy(String branchName, String pom) {
      if (isDeployBranch(branchName)) {
          mvnBuild(pom, "install")
      } else {
          mvnDeploy(pom)
      }
  }  

  /**
   * master和Release分支不进行部署,只上传jar
   * @param branchName
   * @return
   */
  static def isDeployBranch(String branchName) {
      return !(branchName == 'master' || branchName.startsWith('R'))
  }  

  /**
   * 构建docker部署或者Omp部署
   * @param branchName
   */
  def deployOmp(String node) {  
  

      stage('trigger build omp') {
          // 构建omp部署 触发下游job
          // 这里node表示kb-omp项目的分支
          build job: "kb-omp/${node}", wait: false
      }  

      /*if (branchName == 'dev') {
          // build docker image
         *//* stage('docker') {  

              // 并发同时构建docker镜像
              parallel(
                      service: {
                          // service docker
                          sh 'mvn -f service/impl/pom.xml dockerfile:build'
                      },
                      web: {
                          // web docker
                          sh 'mvn -f web/impl/pom.xml dockerfile:build'
                      },
                      gateway: {
                          // OpenApi docker
                          sh 'mvn -f gateway/impl/pom.xml dockerfile:build'
                      }
              )  

          }  

          stage('deploy') {
              // 手动清洗none images
              //sh 'docker rmi $(docker images | grep "^<none>" | awk "{print $3}")'
              sh 'docker-compose --project-name ljtest down --rmi=local'
              sh 'docker-compose --project-name ljtest up -d'  

              // 手动清洗unuse images
              sh 'docker image prune -f'
          }*//*  

          // dev分支部署omp
          stage('trigger build omp') {
              // 构建omp部署 触发下游job
              build job: 'kb-omp/dev', wait: false
          }
      } else if (branchName == 'test') {
          // test分支部署omp
          stage('trigger build omp') {
              // 构建omp部署 触发下游job
              build job: 'kb-omp/test', wait: false
          }
      } else if (branchName == 'prod') {
          // prod环境部署omp
          stage('trigger build omp') {
              // 构建omp部署 触发下游job
              build job: 'kb-omp/prod', wait: false
          }
      }*/
  }  

  // vim: ft=groovy


```

