from .base import *

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': 'django_mobile',
        'USER': 'root',
        'PASSWORD': 'sangjun1324',
        'HOST': 'db',
        'PORT': '3306',
        "OPTIONS": {"charset": "utf8mb4"}
    }
}