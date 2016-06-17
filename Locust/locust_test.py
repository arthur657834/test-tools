# coding=utf-8
from locust import HttpLocust, TaskSet, task
import json


class UserBehavior(TaskSet):

    token = ''
    userId = ''
    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'}

    # 登录
    def login(self):
        data = {
            "email": "xxxx@xxxx.cn",
            "passwd": "xxxx",
        }
        response = self.client.post(
            'tenant/api/v1/user/login',
            data=json.dumps(data),
            headers=self.headers)
        content = json.loads(response.content)
        self.token = {"token": content['data']['token']}
        self.userId = content['data']['userId']

    # 登出
    def logout(self):
        self.client.get('tenant/api/v1/user/logout', params=self.token)

    # 用户详情显示
    def user_details(self):
        data = {'userId': self.userId}
        self.client.get(
            'tenant/api/v1/user/details/view',
            params=data,
            headers=self.headers)

    # 用户list显示
    def user_list(self):
        self.client.get('tenant/api/v1/user/list', headers=self.headers)

    # 用户修改
    def user_update(self):
        data = {
            "userId": self.userId,
            "realname": "李四",
            "email": "xxxxx@xxxx.cn",
            "mobile": "xxxxx",
        }
        self.client.post(
            'tenant/api/v1/user/update',
            data=json.dumps(data),
            headers=self.headers)

    # 产品列表
    def product_listall(self):
        self.client.get('tenant/api/v1/product/listAll', headers=self.headers)

    # 用户在产品中的角色
    def get_user_product(self):
        data = {'userId': self.userId, 'productNum': 1001}
        self.client.get(
            'tenant/api/v1/product/getUserProduct',
            params=data,
            headers=self.headers)

    @task(10)
    def login_logout(self):
        self.login()
        self.user_details()
        self.user_list()
        self.user_update()
        self.product_listall()
        self.get_user_product()
        self.logout()


class WebsiteUser(HttpLocust):
    host = 'http://10.1.50.230:7600/'
    task_set = UserBehavior
    min_wait = 5000
    max_wait = 6000
