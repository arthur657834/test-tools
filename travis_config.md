https://travis-ci.org/


vi .travis.yml
language: python
python:
  - "2.7"

services:
  - mongodb
sudo: required
os:
- linux
- osx
dist: trusty
osx_image: xcode8
cache:
- apt: true
- directories:
  - "$HOME/.npm"
  - "$HOME/.electron"
matrix:
  allow_failures:
  - node_js: '5'
    os: osx
    
env:
  global:
    - "FTP_USER=myusername"
    - "FTP_PASSWORD=mypassword"
    
# Use this to prepare the system to install prerequisites or dependencies
before_install: "sudo apt-get update"
 
# command to install dependencies
install: "pip install -q -r requirements.txt --use-mirrors"

# command to build
script: make html

after_script:
  - cd ./public
  - git init
  - git config user.name "lifengsofts"
  - git config user.email "lifengsofts@gmail.com"
  - git add .
  - git commit -m "Update docs"
  - git push --force --quiet "https://${GH_TOKEN}@${GH_REF}" master:master
# E: Build LifeCycle
  
# whitelist
branches:
  only:
    - mybranch
    - /^v\d+\.\d+\.\d+/
after_success:
    "find ./make/output -type f -exec curl --ftp-create-dirs -u $FTP_USER:$FTP_PASSWORD -T {} ftp://123.45.67.89/myproject/{} \\;"  