
1.
使用slave时可能会导致一些命令找不到

> 注意可能在linux slave上使用了bashrc而非profile导致的

2. 重启jenkins

访问 http://xxxxxxxx:8080/restart 

3.插件列表

https://wiki.jenkins-ci.org/display/JENKINS/Plugins

4.
Parameterized Trigger Plugin:
参数传递
![第1个job配置](./step_1.jpg)
![第2个job配置](./step_2.jpg)

5.
Environment Injector Plugin
Inject environment variables to the build process
![jenkins环境配置](./Inject_environment_variables.png)

6.
thinbackup backup plugin:备份插件
```
thinBackup plugin可以自动备份全局的和job的指定配置文件（不包括archive和workspace）。
backup plugin可以备份JENKINS_HOME，可以选择是否备份workspace、builds history、maven atifacts archives、fingerprints等。
thinBackup plugin和backup plugin不同：
backup plugin只能手动触发备份，thinBackup plugin可以定期自动备份。
backup plugin可以备份JENKINS_HOME，可以选择哪些内容是否需要备份（如workspace、builds history等）， thinBackup plugin只备份最重要的信息（全局的和job的指定配置文件）
```
7.
* Hudson Post build task:增加构建后的操作
* build timeout plugin:构建超时设置
* Build Timestamp Plugin:往console log中增加时间戳

8.
Join plugin
等下游job echo_2和echo_3完成之后才会触发echo_4
![join_job](./join_job.png)

9.
Multiple SCMs plugin

10.
高级项目选项：

* 安静期：一个任务计划中的构建在开始前需要读取开始前的配置信息；不选中则使用系统默认值
* 重复次数：若从版本库签除代码失败，则Jenkins会按指定次数重试后再放弃
* 该项目的上游项目正在构建时阻止该项目构建：当项目有依赖关系时，需要勾选
* 该项目的下游项目正在构建时阻止该项目构建：
* 使用自定义的工作空间：每个Jenkins任务都会有唯一一个工作空间目录，下载源代码，构建的整个过程都是在这个工作空间中
* 显示名称：仅供Jenkins Web页面显示

11. 远程参数化构建
> http://x.x.x.x:8080/jenkins/job/echo_1/buildWithParameters?token=ljtest123&ljtest=Value&ljtest2=true
> http://x.x.x.x:8080/jenkins/job/echo_1/build?token=ljtest123

