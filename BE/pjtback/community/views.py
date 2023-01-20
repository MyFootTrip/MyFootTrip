import datetime
from django.db.models import F
from rest_framework.response import Response
from rest_framework.decorators import api_view
from rest_framework import status
import requests

# permission Decorators
from rest_framework.decorators import permission_classes
from rest_framework.permissions import IsAuthenticated

from django.shortcuts import get_object_or_404, get_list_or_404
# from .serializers import  CommunityListSerializer, CommunitySerializer, CommentSerializer, ArticleImageSerializer , TravelPathSerializer, LikeSerializer, CommunityCreateSerializer
from .serializers import BoardListSerializer, PlaceSerializer, TravelSerializer
from .models import Board, Place, Travel
from django.contrib.auth import get_user_model

# json 파싱을 위해서
import json

# for swagger
from drf_spectacular.utils import extend_schema, OpenApiParameter, OpenApiTypes, OpenApiExample, inline_serializer
from rest_framework.decorators import api_view
from rest_framework import serializers


# for db orm query
from django.db.models import Q


@extend_schema(responses=BoardListSerializer(many=True), summary='게시글 전체 가져오기')
@api_view(['GET'])
def board_get(request):
    boards = Board.objects.all()
    serializer = BoardListSerializer(boards, many=True)
    return Response(serializer.data)


@extend_schema(request=BoardListSerializer(), summary='게시글 생성')
@api_view(['POST'])
def board_create(request):
    User = get_user_model()
    user = User.objects.get(pk=request.data['userId'])
    
    serializer = BoardListSerializer(data=request.data)
    wanted_travel = get_object_or_404(Travel, pk=request.data['travel']['travelId'])

    if serializer.is_valid(raise_exception=True):
        serializer.save(userId=user, travel_id=wanted_travel)
        return Response(serializer.data, status=status.HTTP_201_CREATED)


# 쿼리 형식으로 db 접근 할 수 있는 라이브러리
from django.db.models import Q, F
from datetime import timedelta

@extend_schema(summary='게시글 필터 필터 부분 바디에 담아서 보내주시면 됨')
@api_view(['POST'])
def board_filtered(request):
    boards = Board.objects.annotate(
        day=F('travel__endDate') - F('travel__startDate'))
    # 기본 전략은 4가지 필터 부분으로 분리하고, get 으로 접근
    # get 했을 때 none 이면 필터 pass 하는 식
    age = request.data.get('ageList')
    periods = request.data.get('periodList')
    theme = request.data.get('themeList')
    region = request.data.get('regionList')

    boards = Board.objects.annotate(day = F('travel__endDate') - F('travel__startDate'))

    # 일단 하드 코딩에 가깝긴 한데, 프엔에서 넘겨주는 타이밍 까지 만드는게 아니면 그냥 이런식으로 쓰고 수정하는게 나을듯?
    age_dic = { '10대' : [10,20], '20대': [20,30] , '30대' : [30,40] , '40대': [40,50], '50대 이상': [50,100]}
    periods_dic = {'당일치기': 1, '1박2일': 2, '3박4일': 3, '4박5일+': 4}
    theme_lst = ['혼자여행', '커플여행', '효도여행', '우정여행', '직장여행']
    region_lst = ["서울","경기","강원","부산","경북·대구","전남·광주","제주","충남·대전","경남","충북","경남","전북","인천"]


    # alyways true q object 찾아보니까 이렇더라 이거 default로 추가해가면 될 것 같음.
    # result_query = ~Q(pk__in=[])
    age_query = Q(pk__in=[])
    period_query = Q(pk__in=[])
    theme_query = Q(pk__in=[])
    region_query = Q(pk__in=[])

    if age:
        for age_str in age:
            age_query  |= Q(userId__age__gte = age_dic[age_str][0] , userId__age__lt = age_dic[age_str][1])
    else:
        age_query = ~Q(pk__in=[])
    
    if periods:
        for period_str in periods:
            period_query |= Q(day__lt = timedelta(days=periods_dic[period_str]))
    else:
        period_query = ~Q(pk__in=[])

    if theme:
        for theme_str in theme:
            theme_query |= Q(theme = theme_str)
    else:
        theme_query = ~Q(pk__in=[])

    if region:    
        for region_str in region:
            region_query |= Q(travel__location__contains = region_str)
    else:
        region_query = ~Q(pk__in=[])
    
    result_query = age_query & period_query & theme_query & region_query
    result_board = boards.filter(result_query)

    print(result_query)

    serializer = BoardListSerializer(result_board, many= True)

    return Response(serializer.data, status= status.HTTP_200_OK)


@api_view(['GET'])
def travel_get(request):
    travels = Travel.objects.all()
    serializer = TravelSerializer(travels, many=True)
    return Response(serializer.data)

@api_view(['GET', 'PUT', 'DELETE'])
def travel_detail(request, travel_id):
    travel = get_object_or_404(Travel, id = travel_id)
    if request.method == 'GET':
        serializer = TravelSerializer(travel)
        return Response(serializer.data)

    elif request.method == 'PUT':
        # user = request.user
        # if travel.userId == user:
        User = get_user_model()
        user = User.objects.get(id=1)
        serializer = TravelSerializer(travel, data=request.data)
        if serializer.is_valid(raise_exception=True):
            serializer.save(userId = user)
            
            return Response(serializer.data, status=status.HTTP_201_CREATED)  
    
    elif request.method == 'DELETE':
        # if request.user == travel.userId:
        #     travel.delete()
        #     return Response(status=status.HTTP_204_NO_CONTENT)
        # else:
        #     return Response(status=status.HTTP_401_UNAUTHORIZED)
        travel.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

@api_view(['POST'])
def travel_create(request):
    # user = request.user
    User = get_user_model()
    user = User.objects.get(id=1)
    serializer = TravelSerializer(data=request.data, context ={'request': request})
    if serializer.is_valid(raise_exception=True):
        serializer.save(userId=user)

        return Response(serializer.data, status=status.HTTP_201_CREATED)

@api_view(['GET'])
def travel_user(request, user_id):
    travels = Travel.objects.filter(userId__id = user_id)
    serializer = TravelSerializer(travels, many = True)

    return Response(serializer.data)
        








# 밑에 주석은 참고 사항 때매 남겨 놓은것 나중에 지우겠습니다. get, post, put , delete 와 swagger 를 위해 함수 2개만 남겨 두었습니다.
# @extend_schema(request=CommunitySerializer,responses=CommunitySerializer,summary='게시글 상세, 삭제, 수정')
# @api_view(['GET', 'PUT', 'DELETE'])
# def community_detail(request, community_pk):
#     community = get_object_or_404(Community, pk=community_pk)

#     if request.method == 'GET':
#         serializer = CommunitySerializer(community)
#         return Response(serializer.data)

#     elif request.method == 'DELETE':
#         community.delete()
#         return Response(status=status.HTTP_204_NO_CONTENT)

#     elif request.method == 'PUT':
#         serializer = CommunitySerializer(community, data=request.data)
#         if serializer.is_valid(raise_exception=True):
#             serializer.save(user=request.user)
#             return Response(serializer.data)

# @extend_schema(responses = CommentSerializer , request=None ,summary='코멘트 생성')
# @api_view(['POST'])
# def comment_create(request, community_pk):
#     community = Community.objects.get(pk=community_pk)
#     serializer = CommentSerializer(data=request.data)
#     if serializer.is_valid(raise_exception=True):
#         serializer.save(community=community, user=request.user)
#         return Response(serializer.data, status=status.HTTP_201_CREATED)

