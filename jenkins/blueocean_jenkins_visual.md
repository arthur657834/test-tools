
https://jenkins.io/doc/book/blueocean/getting-started/ <br>
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
