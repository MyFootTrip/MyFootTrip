from django.db import models
from django.conf import settings
from django.utils.translation import gettext_lazy as _
from accounts.models import User
from imagekit.models import ProcessedImageField
from imagekit.processors import ResizeToFill


    
class Travel(models.Model):
    userId = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='travel')
    location = models.JSONField()
    startDate = models.DateTimeField(auto_now=False, auto_now_add=False)
    endDate = models.DateTimeField(auto_now=False, auto_now_add=False)
    
class Board(models.Model):
    userId = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='writeBoard')
    writeDate = models.DateTimeField(auto_now = True)
    theme = models.CharField(max_length=20, default='')
    title = models.CharField(max_length= 50 , default="")
    content = models.TextField(max_length=300)
    imageList = models.JSONField()
    travel = models.ForeignKey(Travel, on_delete=models.CASCADE, related_name='board')
    likeList = models.ManyToManyField(User, related_name='myLikeBoard', blank= True, through='Like')

class Place(models.Model):
    travel = models.ForeignKey(Travel, on_delete=models.CASCADE, related_name='placeList')
    placeName = models.CharField(max_length = 20, default="대구")
    saveDate = models.DateTimeField(auto_now=False, auto_now_add=False)
    memo = models.CharField(max_length=20)
    # placeImgList = models.ManyToManyField("PlaceImage", related_name='place')
    latitude = models.FloatField(default=0.0)
    longitude = models.FloatField(default=0.0)
    address = models.CharField(max_length=255, default='')

class PlaceImage(models.Model):
    place = models.ForeignKey(Place, related_name='placeImgList', on_delete=models.CASCADE)
    picture = ProcessedImageField(
        blank=True,
        upload_to='palce_image/%Y/%m',
        processors=[],
        format='JPEG',
        options={'quality': 70},
        null= True
    )

class Comment(models.Model):
    board = models.ForeignKey(Board, on_delete=models.CASCADE, related_name='commentList')
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="commentList")
    content = models.TextField(max_length=50)
    write_date = models.DateTimeField(auto_now_add=True)
    message = models.CharField(max_length= 50, default='')

class Like(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    board = models.ForeignKey(Board, on_delete=models.CASCADE)

class Notification(models.Model):
    creator = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='creatorList')
    to = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='toList')
    msg = models.CharField(_("message"), max_length=100)
    notification_type = models.IntegerField(default=0)
    createdate = models.DateTimeField(auto_now_add=True)
