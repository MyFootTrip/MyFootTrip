### 우분투 초기 설정 ###
# ubuntu 이미지 22.04 버전을 베이스 이미지로 한다
FROM ubuntu:22.04

# apt 업그레이트 및 업데이트
RUN apt-get -y update && apt-get -y dist-upgrade
# debconf : apt-util 어쩌구 빨간 error 방지
RUN apt-get install -y --no-install-recommends apt-utils
RUN apt-get install -y python3-pip python3-dev

# Django 기본 언어를 한국어로 설정하면 파이썬 기본 인코딩과 충돌되어 한글 출력, 입력시에 에러가 난다.
# 따라서 파이썬 기본 인코딩을 한국어를 사용할 수 있는 utf-8으로 설정한다.
ENV PYTHONIOENCODING=utf-8

# pip 를 root user 로 전부 해결 하는 것에 대한 warnign ignore 옵션
# 근본적으로는 user 만들어서 image 처리 하는게 맞다.. 나중에 고려할것 
ENV PIP_ROOT_USER_ACTION=ignore

# frontend 어쩌구 하는 error 해결 , input 필요한 install 부분 때매 생겨난다 예시 : tzdata
ENV DEBIAN_FRONTEND=noninteractive

# pip setuptools 업그레이드
# mysql 붙이기 위한 작업
RUN pip3 install --upgrade pip
RUN pip3 install --upgrade setuptools
RUN apt-get install -y python3-dev default-libmysqlclient-dev build-essential

### 실행 환경 구축 ###
# 컨테이너 내부에 config(설정파일)가 들어갈 폴더 생성
RUN mkdir /config
ADD /config/requirements.txt /config/

# requirements.txt에 있는 파이썬 패키지 설치
RUN pip3 install -r /config/requirements.txt
RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime
RUN apt-get install tzdata 

### 작업 디렉토리 ###
# Django 소스코드가 들어갈 폴더 생성
RUN mkdir /pjtback;
WORKDIR /pjtback