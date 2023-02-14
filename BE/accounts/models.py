from django.db import models
from django.contrib.auth.models import AbstractUser, PermissionsMixin
from imagekit.models import ProcessedImageField
from imagekit.processors import ResizeToFill
from django.utils.translation import gettext_lazy as _
from django.conf import settings

# Create your models here.

class User(AbstractUser):
    email = models.EmailField(_("email_address"), unique=True)
    username = models.CharField(max_length=50, unique=False)
    phone_number = models.CharField(_("phone"), max_length=13,)
    profileImg = ProcessedImageField(
        blank=True,
        upload_to='profile_image/accounts/%Y/%m',
        processors=[ResizeToFill(300, 300)],
        format='JPEG',
        options={'quality': 70},
        null= True
    )
    nickname = models.CharField(_("nickname"), max_length=50)
    age = models.IntegerField(_("age"), null=True)
    naver = models.EmailField(_("naver_email"), null=True, blank=True)
    kakao = models.EmailField(_("kakao_email"), null=True, blank=True)
    google = models.EmailField(_("google_email"), null=True, blank=True)
    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = []

class EmailValidateModel(models.Model):
    email = models.EmailField()
    validateNumber = models.CharField(max_length=10)

class FireBase(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, null=True)
    fcmToken = models.CharField("FCM Token", blank=True, max_length=500, null=True)