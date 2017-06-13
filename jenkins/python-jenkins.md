```shell
pip install python-jenkins
```

```python
#!/usr/bin/python
# -*- coding: UTF-8 -*-
import jenkins
import xml.etree.ElementTree as ET
import sys
reload(sys)
sys.setdefaultencoding("utf-8")

jenkins_server_url='http://10.1.3.99:8080/jenkins'
user_id='admin'
api_token='2ad0d620fbf6fbd2085d20b25b1388bf'
notify_job_name='DingNotify'
server=jenkins.Jenkins(jenkins_server_url, username=user_id, password=api_token)
server.get_job_info(notify_job_name)

job_list=server.get_jobs()
for i in range(len(job_list)):
  print job_list[i]['name']
  if job_list[i]['name'] != notify_job_name:
    if server.get_job_info(job_list[i]['name'])['firstBuild'] != None:
      build_number = server.get_job_info(job_list[i]['name'])['lastBuild']['number']
      print server.get_build_info(job_list[i]['name'], build_number)['result']
      if server.get_build_info(job_list[i]['name'], build_number)['result'] == 'FAILURE':
        root = ET.fromstring(server.get_job_config(job_list[i]['name']))
        if root.find("publishers/org.yaoyao.jenkins.dingnotify.DingNotifier/userLists")!= None:
          dinguser = root.find("publishers/org.yaoyao.jenkins.dingnotify.DingNotifier/userLists").text
          print dinguser
          server.build_job(notify_job_name,parameters={"dinguser":dinguser})
```
