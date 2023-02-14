from rest_framework import serializers
from .models import Board, Travel, Place, Comment, Like, Notification, PlaceImage
from rest_framework.response import Response
from rest_framework import status
from rest_framework.exceptions import ValidationError, ParseError
from django.utils.timezone import now, timedelta
import json
import datetime

class PlaceImageSerializer(serializers.ModelSerializer):
    url = serializers.ImageField(source = "picture", use_url = True)

    class Meta:
        model = PlaceImage
        fields = ("url",)

    def to_representation(self, instance):
        url = instance.picture.url
        request = self.context.get('request', None)
        if request is not None:
            return request.build_absolute_uri(url)
        return url


class PlaceSerializer(serializers.ModelSerializer):
    placeId = serializers.IntegerField(source='id', read_only=True)
    saveDate = serializers.DateTimeField(format="%Y-%m-%d %H:%M:%S")
    placeImgList = PlaceImageSerializer(required=False, allow_null=True, many=True)

    class Meta:
        model = Place
        fields = ('placeId', 'placeName', 'saveDate', 'memo',
                  'placeImgList', 'latitude', 'longitude', 'address',)


class TravelSerializer(serializers.ModelSerializer):
    # userId = serializers.IntegerField(source='userId.id', read_only=True)
    travelId = serializers.IntegerField(
        source='id', required=False, read_only=True)
    placeList = PlaceSerializer(
        many=True, required=False, allow_null=True, read_only=True)
    startDate = serializers.DateTimeField(format="%Y-%m-%d %H:%M:%S")
    endDate = serializers.DateTimeField(format="%Y-%m-%d %H:%M:%S")

    class Meta:
        model = Travel
        fields = ('travelId', 'location', 'startDate', 'endDate', 'placeList',)

    def create(self, validated_data):
        places = self.context['request'].data['placeList']
        images = self.context['request'].FILES.getlist('placeImgList')
        places = json.loads(places)

        if places:
            instance = Travel.objects.create(**validated_data)
            for idx, place in enumerate(places):
                # 플레이스 생성
                print(place)
                new_place = Place.objects.create(travel=instance, placeName = place["placeName"], saveDate = place["saveDate"], memo = place["memo"], latitude = place["latitude"], longitude = place["longitude"], address = place["address"])
                # 이미지 존재할 때 플레이스 이미지 컬럼 생성
                if images:
                    for image in images:
                        if image.name.split('_')[0] == str(idx):
                            PlaceImage.objects.create(place = new_place, picture = image)

        else:
            raise ValidationError

        return instance

    def update(self, instance, validated_data):
        instance.location = validated_data.get('location', instance.location)
        instance.startDate = validated_data.get(
            'startDate', instance.startDate)
        instance.endDate = validated_data.get('endDate', instance.endDate)

        
        # 일단은 관련 플레이스 죄다 삭제 후 재생성으로 했습니다.
        # 단시간에 알고리즘 짜면 for 3번 돌거 같아서...
        d_places = json.loads(self.context['request'].data['DeletePlaceList'])
        if d_places:
            for d_place in d_places:
                Place.objects.filter(id=d_place).delete()
        d_pictures = json.loads(self.context['request'].data['DeleteImageList'])
        if d_pictures:
            for d_picture in d_pictures:
                PlaceImage.objects.filter(picture=d_picture).delete()
        places = json.loads(self.context['request'].data['placeList'])
        images = self.context['request'].FILES.getlist('placeImgList')
        if places:
            for idx, place in enumerate(places):
                try:
                    update_place = Place.objects.get(id=place.get("placeId"))
                    update_place.travel=instance
                    update_place.placeName=place['placeName']
                    update_place.saveDate=place['saveDate']
                    update_place.memo=place['memo']
                    update_place.latitude=place['latitude']
                    update_place.longitude=place['longitude']
                    update_place.address=place['address']
                    update_place.save()
                except Place.DoesNotExist:
                    update_place = Place.objects.create(travel=instance, placeName = place["placeName"], saveDate = place["saveDate"], memo = place["memo"], latitude = place["latitude"], longitude = place["longitude"], address = place["address"])
                if images:
                    for image in images:
                        if image.name.split('_')[0] == str(idx):
                            PlaceImage.objects.create(place = update_place, picture = image)

        instance.save()
        return instance


class CommentSerializer(serializers.ModelSerializer):
    commentId = serializers.IntegerField(source='id', read_only=True)
    boardId = serializers.IntegerField(source='board.pk', read_only=True)
    profileImg = serializers.ImageField(
        source='user.profileImg', read_only=True, use_url=True)
    userId = serializers.CharField(source='user.pk', read_only=True)
    nickname = serializers.CharField(source='user.nickname', read_only=True)
    writeDate = serializers.DateTimeField(
        format="%Y-%m-%d %H:%M:%S", source='write_date', read_only=True)
    message = serializers.CharField(read_only=True, required=False)

    class Meta:
        model = Comment
        fields = ('commentId', 'boardId', 'profileImg', 'userId',
                  'nickname', 'content', 'writeDate', 'message')
        read_only_fields = ('user', 'board', 'profileImg', 'message')


class BoardListSerializer(serializers.ModelSerializer):
    boardId = serializers.IntegerField(source='id', read_only=True)
    userId = serializers.IntegerField(source='userId.pk', read_only=True)
    nickname = serializers.CharField(source='userId.nickname', read_only=True)
    profileImg = serializers.ImageField(
        source='userId.profileImg', read_only=True, use_url=True)
    writeDate = serializers.DateTimeField(
        format="%Y-%m-%d %H:%M:%S", read_only=True)
    travel = TravelSerializer(read_only=True)
    imageList = serializers.JSONField(required=False)
    commentList = CommentSerializer(
        many=True, required=False, allow_null=True, read_only=True)

    class Meta:
        model = Board
        fields = ('boardId', 'userId', 'nickname', 'profileImg', 'writeDate', 'theme',
                  'title', 'content', 'imageList', 'travel', 'likeList', 'commentList')
        read_only_fields = ('userId', 'travel', 'profileImg', 'writeDate')


class LikeSerializer(serializers.ModelSerializer):

    class Meta:
        model = Like
        fields = '__all__'


class NotificationSerializer(serializers.ModelSerializer):
    notificationId = serializers.IntegerField(source='id', read_only=True)
    notificationType = serializers.IntegerField(source='notification_type')
    profileImg = serializers.ImageField(
        source='creator.profileImg', read_only=True, use_url=True)
    message = serializers.CharField(source='msg')
    createDate = serializers.DateTimeField(source='createdate',format="%Y-%m-%d %H:%M:%S", read_only=True)

    class Meta:
        model = Notification
        fields = ('notificationId', 'message',
                  'profileImg', 'notificationType', 'createDate')

