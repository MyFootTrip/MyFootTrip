FROM python:3.10.6

LABEL lumicontainers.image.authors="luminaries1@naver.com"

RUN apt-get -y update

RUN mkdir /srv/docker-django
# 현재의 directory를 /srv/docker-django로 옮기기
ADD . /srv/docker-django

# WORKDIR /usr/src/app
WORKDIR /srv/docker-django

# COPY requirements.txt ./
RUN pip install --upgrade pip
RUN pip install -r requirements.txt
# COPY . .

EXPOSE 8000
CMD ["python", "manage.py", "runserver", "0.0.0.0:8000"]