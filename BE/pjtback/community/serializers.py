from rest_framework import serializers
from .models import Board, Travel ,Place , Comment, Like
from rest_framework.response import Response
from rest_framework import status 

class PlaceSerializer(serializers.ModelSerializer):
    placeId = serializers.IntegerField(source='id', read_only = True)
    saveDate = serializers.DateTimeField(format="%Y-%m-%d %H:%M:%S", read_only= True)
    placeImgList = serializers.JSONField(required=False)

    class Meta:
        model = Place
        fields = ('placeId','placeName','saveDate','memo','placeImgList','latitude','longitude','address',)


class TravelSerializer(serializers.ModelSerializer):
    # userId = serializers.IntegerField(source='userId.id', read_only=True)
    travelId = serializers.IntegerField(source='id', required=False, read_only=True)
    placeList = PlaceSerializer(many=True, required = False, allow_null = True ,read_only=True)
    startDate = serializers.DateTimeField(format="%Y-%m-%d %H:%M:%S")
    endDate = serializers.DateTimeField(format="%Y-%m-%d %H:%M:%S")

    class Meta:
        model = Travel
        fields = ( 'travelId','location','startDate','endDate','placeList',)
    
    def create(self, validated_data):
        instance = Travel.objects.create(**validated_data)

        places = self.context['request'].data['placeList']
        if places:
            for place in places:
                new_place = Place.objects.create(travel = instance, **place)

        return instance
        
class CommentSerializer(serializers.ModelSerializer):
    commentId = serializers.IntegerField(source='id', read_only=True)
    boardId = serializers.IntegerField(source='board.pk', read_only=True)
    userId = serializers.CharField(source='user.pk', read_only=True)
    nickname = serializers.CharField(source = 'user.nickname', read_only = True)
    profileImg = serializers.ImageField(source = 'user.profileImg')

    class Meta:
        model = Comment
        fields = '__all__'
        read_only_fields = ('user',)

class BoardListSerializer(serializers.ModelSerializer):
    boardId = serializers.IntegerField(source='id', read_only=True)
    userId = serializers.IntegerField(source ='userId.pk', read_only=True)
    nickname =  serializers.CharField(source='userId.nickname', read_only=True)
    profileImg = serializers.ImageField(source = 'user.profileImg', read_only= True)
    writeDate = serializers.DateTimeField(format="%Y-%m-%d %H:%M:%S", read_only= True)
    travel = TravelSerializer(read_only = True)
    imageList = serializers.JSONField(required=False)
    commentList = CommentSerializer(many=True, required = False, allow_null = True ,read_only=True)

    class Meta:
        model = Board
        fields = ('boardId','userId','nickname', 'profileImg','writeDate','theme','title','content','imageList','travel','likeList','commentList')
        read_only_fields = ('userId','travel','profileImg','writeDate')




class LikeSerializer(serializers.ModelSerializer):

    class Meta:
        model = Like
        fields = '__all__'