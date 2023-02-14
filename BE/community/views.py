from django.db.models import F
from rest_framework.response import Response
from rest_framework.decorators import api_view
from rest_framework import status

# permission Decorators
from rest_framework.decorators import permission_classes
from rest_framework.permissions import IsAuthenticated

from django.shortcuts import get_object_or_404, get_list_or_404
from .serializers import BoardListSerializer, PlaceSerializer, TravelSerializer , CommentSerializer, NotificationSerializer
from .models import Board, Place, Travel, Comment, Notification
from accounts.models import FireBase
from django.contrib.auth import get_user_model

# json 파싱을 위해서
import json

# for swagger
from drf_spectacular.utils import extend_schema, OpenApiParameter, OpenApiTypes, OpenApiExample, inline_serializer
from rest_framework.decorators import api_view
from rest_framework import serializers

# for db orm query
from django.db.models import Q

# for firebase messaging
from firebase_admin import messaging

# redis cache
from django.core.cache import cache
from django.db.models import Count


# redis caching  ㅠㅠ
# @extend_schema(responses=BoardListSerializer(many=True), summary='게시글 전체 가져오기')
# @api_view(['GET'])
# def board_get(request):
#     all_boards = cache.get('all_boards')
#     if not all_boards:
#         boards = Board.objects.all()
#         serializer = BoardListSerializer(boards, many=True, context={"request": request})
#         cache.set('all_boards', serializer.data)
#         all_boards = serializer.data
#     return Response(all_boards)

# fire base message를 위한 함수
def send_to_firebase_cloud_messaging(send_content, send_token):
    message = messaging.Message(
    notification=messaging.Notification(
        title=send_content,
    ),
    token=send_token,
    )

    try:
        response = messaging.send(message)
    except:
        print('옛날 토큰입니다.')
        FireBase.objects.filter(fcmToken = send_token ).delete()

def filtered_board(age,periods,theme,region,sorted_type):
    boards = Board.objects.annotate(day = F('travel__endDate') - F('travel__startDate'))

    # 일단 하드 코딩에 가깝긴 한데, 프엔에서 넘겨주는 타이밍 까지 만드는게 아니면 그냥 이런식으로 쓰고 수정하는게 나을듯?
    age_dic = { '10대' : [10,20], '20대': [20,30] , '30대' : [30,40] , '40대': [40,50], '50대': [50,60], '60대 이상': [60,100]}
    periods_dic = {'당일 치기': 1, '1박 2일': 2, '2박 3일': 3, '3박 4일': 4, '4박 5일+': 4}
    theme_lst = ['혼자', '친구와', '연인과', '배우자와', '아이와', '부모님과', '기타']
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
            if period_str == "4박 5일+":
                period_query |= Q(day__gte = timedelta(days=4))
            else:
                period_query |= Q(day__lt = timedelta(days=periods_dic[period_str])) & Q(day__gte = timedelta(days=periods_dic[period_str]-1))

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


    if sorted_type :
        # result_boards = sorted(result_board, key= lambda x: len(x.like) )
        result_boards = result_board.annotate(like_count=Count('likeList')).order_by('-like_count')
    else:
        result_boards = result_board.order_by('-writeDate')
        # result_boards = sorted(result_board, key=lambda x: x.writeDate)
    return result_boards


@extend_schema(responses=BoardListSerializer(many=True), summary='게시글 전체 가져오기')
@api_view(['GET'])
def board_get(request):
    
    if request.GET.get('page'):
        page_num = int(request.GET.get('page'))
    else:
        page_num = 1
    if request.GET.get('periodList'):
        periodList = request.GET.getlist('periodList')
    else:
        periodList = []
    if request.GET.get('ageList'):
        ageList = request.GET.getlist('ageList')
    else:
        ageList = []
    if request.GET.get('themeList'):
        themeList = request.GET.getlist('themeList')
    else:
        themeList = []
    if request.GET.get('regionList'):
        regionList = request.GET.getlist('regionList')
    else:
        regionList = []
    if request.GET.get('sortedType') :   
        sortedType = int(request.GET.get('sortedType'))
    else:
        sortedType = 0

    result_boards = filtered_board(ageList, periodList, themeList, regionList, sortedType)

    # pagination size 하드코딩 부분 ㅠ
    page_size = 25

    if page_size*page_num < len(result_boards):
        paging_boards = result_boards[page_size*(page_num-1):page_size*(page_num)]
    elif page_size*(page_num-1) < len(result_boards):
        paging_boards = result_boards[page_size*(page_num-1):]
    else:
        return Response(data=[])
    
    serializer = BoardListSerializer(paging_boards, many=True, context={"request": request})
    return Response(serializer.data)


@extend_schema(responses=BoardListSerializer(), request=BoardListSerializer(), summary='게시글 생성')
@api_view(['POST'])
@permission_classes([IsAuthenticated])
def board_create(request):
    user = request.user
    
    serializer = BoardListSerializer(data=request.data)
    wanted_travel = get_object_or_404(Travel, pk=request.data['travel']['travelId'])
    
    if serializer.is_valid(raise_exception=True):
        serializer.save(userId=user, travel=wanted_travel)
        return Response(serializer.data, status=status.HTTP_201_CREATED)


# 쿼리 형식으로 db 접근 할 수 있는 라이브러리
from django.db.models import Q, F
from datetime import timedelta


# @extend_schema(request=inline_serializer(name="gamsa",
#     fields={
#         "ageList": serializers.ListField(child=serializers.CharField()),
#         "periodList" :serializers.ListField(),
#         "themeList" : serializers.ListField(),
#         "regionList": serializers.ListField()
#     }), responses=BoardListSerializer(many=True) ,summary='게시글 필터 필터 부분 바디에 담아서 보내주시면 됨')
# @api_view(['POST'])
# def board_filtered(request):
#     # result_boards = filtered_board(age,periods,theme,region,sorted_type )
#     # serializer = BoardListSerializer(result_boards, many= True, context={"request": request})

#     return Response(data= [], status= status.HTTP_200_OK)

@extend_schema(summary='Board 상세페이지 조회, 수정, 삭제')
@api_view(['GET', 'PUT', 'DELETE'])
@permission_classes([IsAuthenticated])
def board_detail(request, board_id):

    board = get_object_or_404(Board, id = board_id)
    if request.method == 'GET':
        serializer = BoardListSerializer(board, context={"request": request})
        return Response(serializer.data)

    elif request.method == 'PUT':
        user = request.user
        if board.userId == user:
            wanted_travel = get_object_or_404(Travel, pk=request.data['travel']['travelId'])
            serializer = BoardListSerializer(board, data=request.data, context={"request": request})
            if serializer.is_valid(raise_exception=True):
                serializer.save(userId=user, travel=wanted_travel)
                
            return Response(serializer.data, status=status.HTTP_201_CREATED)  
    
    elif request.method == 'DELETE':
        if request.user == board.userId:
            board.delete()
            return Response(data="OK",status=status.HTTP_200_OK)
        else:
            return Response(data="뭔가 문제 있음",status=status.HTTP_401_UNAUTHORIZED)

@extend_schema(summary='토큰 넣어주면 해당 유저의 보드 객체들을 얻어 옴')
@api_view(['GET'])
@permission_classes([IsAuthenticated])
def user_board(request):
    user_id = request.user.id
    boards = Board.objects.filter(userId__id = user_id).order_by('-writeDate')
    serializer = BoardListSerializer(boards, many = True, context={"request": request})

    return Response(serializer.data)

@extend_schema(summary='토큰 넣어주면 해당 유저가 좋아하는 보드 객체들을 얻어 옴')
@api_view(['GET'])
@permission_classes([IsAuthenticated])
def user_like_board(request):
    user_id = request.user.id
    boards = Board.objects.filter(likeList__id = user_id).order_by('-like__id')
    serializer = BoardListSerializer(boards, many = True, context={"request": request})

    return Response(serializer.data)

# @api_view(['GET'])
# def board_page(request):
#     boards = Board.objects.all()
#     page_num = int(request.GET.get('page'))
#     if 10*page_num < len(boards):
#         paging_boards = boards[10*(page_num-1):10*(page_num)]
#     elif 10*(page_num-1) < len(boards):
#         paging_boards = boards[10*(page_num-1):]
#     else:
#         return Response(data=[])
    

#     serializer = BoardListSerializer(paging_boards, many=True, context={"request": request})
#     return Response(serializer.data)


@extend_schema(responses=TravelSerializer(many = True), summary='게시글 전체 조회')
@api_view(['GET'])
def travel_get(request):
    travels = Travel.objects.all()
    serializer = TravelSerializer(travels, many=True, context={"request": request})
    return Response(serializer.data)

@extend_schema(request=TravelSerializer(), summary='단일 게시글 조회 수정 삭제')
@api_view(['GET', 'PUT', 'DELETE'])
@permission_classes([IsAuthenticated])
def travel_detail(request, travel_id):
    travel = get_object_or_404(Travel, id = travel_id)
    if request.method == 'GET':
        serializer = TravelSerializer(travel, context={"request": request})
        return Response(serializer.data)

    elif request.method == 'PUT':
        user = request.user
        if travel.userId == user:
            serializer = TravelSerializer(travel, data=request.data, context ={'request': request})
            if serializer.is_valid(raise_exception=True):
                serializer.save(userId = user)
                
                return Response(serializer.data, status=status.HTTP_201_CREATED)
        else:
            return Response(status=status.HTTP_401_UNAUTHORIZED)
    
    elif request.method == 'DELETE':
        if request.user == travel.userId:
            travel.delete()
            return Response(status=status.HTTP_204_NO_CONTENT)
        else:
            return Response(status=status.HTTP_401_UNAUTHORIZED)


@extend_schema(request=TravelSerializer(), summary='여정 생성')
@api_view(['POST'])
@permission_classes([IsAuthenticated])
def travel_create(request):
    user = request.user
    serializer = TravelSerializer(data=request.data, context ={'request': request})
    if serializer.is_valid(raise_exception=True):
        serializer.save(userId=user)

        return Response(status=status.HTTP_201_CREATED)


@extend_schema(summary='user_id 파라미터로 넣어 주면 해당 유저의 travel')
@api_view(['GET'])
def travel_user(request, user_id):
    travels = Travel.objects.filter(userId__id = user_id)
    serializer = TravelSerializer(travels, many = True, context={"request": request})

    return Response(serializer.data)

@extend_schema(summary='좋아요 기능')
@api_view(['POST'])
@permission_classes([IsAuthenticated])
def like(request, board_id):

    board = Board.objects.get(id = board_id)
    user = request.user

    if board.likeList.filter(id=request.user.id).exists():
        board.likeList.remove(user)
        return Response(data = False,status=status.HTTP_202_ACCEPTED)
    else:
        board.likeList.add(user)

        if Notification.objects.filter(msg = request.data["message"]) or user.id == board.userId.id:
            pass
        else:
            # 알림 객체 생성 부분
            notification_serializer = NotificationSerializer(data={"notificationType": 0 , "message" : request.data["message"]}, context={"request": request})
            if notification_serializer.is_valid(raise_exception=True):
                notification_serializer.save(creator=user, to=board.userId)
        
            # 알림을 실제로 보내는 부분
            fcm_list = [firebase for firebase in FireBase.objects.filter(user__id = board.userId.id) ]
            for fcm in fcm_list:
                send_to_firebase_cloud_messaging(request.data['message'], fcm.fcmToken)

        return Response(data = True, status=status.HTTP_202_ACCEPTED)


@extend_schema(responses = CommentSerializer , request=CommentSerializer ,summary='코멘트 생성')
@api_view(['POST'])
@permission_classes([IsAuthenticated])
def comment_create(request, board_id):

    board = Board.objects.get(id=board_id)
    serializer = CommentSerializer(data=request.data)
    user = request.user

    if serializer.is_valid(raise_exception=True):
        serializer.save(board=board, user=request.user)
        board_modified = Board.objects.get(id = board_id)
        boardserializer = BoardListSerializer(board_modified, context={"request": request})

        if user.id == board.userId.id:
            pass
        else:
            notification_serializer = NotificationSerializer(data={"notificationType": 1 , "message" : request.data["message"]}, context={"request": request})
            if notification_serializer.is_valid(raise_exception=True):
                notification_serializer.save(creator=user,to = board.userId)

            fcm_list = [firebase for firebase in FireBase.objects.filter(user__id = board_modified.userId.id) ]
            for fcm in fcm_list:
                send_to_firebase_cloud_messaging(request.data['message'], fcm.fcmToken)

        return Response(boardserializer.data, status=status.HTTP_201_CREATED)

@extend_schema(responses = CommentSerializer , request=CommentSerializer ,summary='코멘트 수정, 삭제')
@api_view(['DELETE', 'PUT'])
@permission_classes([IsAuthenticated])
def comments(request,board_id,comment_id):
    comment = get_object_or_404(Comment, id=comment_id)

    if request.method == 'DELETE':
        comment.delete()
        board_modified = Board.objects.get(id = board_id)
        boardserializer = BoardListSerializer(board_modified, context={"request": request})
        return Response(boardserializer.data, status=status.HTTP_200_OK)
    
    elif request.method == 'PUT':
        comment = Comment.objects.get(id=comment_id)
        board = Board.objects.get(id = board_id)
        serializer = CommentSerializer(comment, data=request.data)
        if serializer.is_valid(raise_exception=True):
            serializer.save(user=request.user, board=board)
            board_modified = Board.objects.get(id = board_id)
            boardserializer = BoardListSerializer(board_modified, context={"request": request})
            return Response(boardserializer.data, status=status.HTTP_202_ACCEPTED)

# 나중에 마이페이지 쪽으로 빼던가 하자
@extend_schema(summary='알림페이지 GET')
@api_view(['GET'])
@permission_classes([IsAuthenticated])
def notification(request):
    user_id = request.user.id
    notifications = Notification.objects.filter(to__id = user_id).order_by('-createdate')
    serializer = NotificationSerializer(notifications, many = True, context={"request": request})    

    return Response(serializer.data)

@extend_schema(summary='알림페이지 count 따로 api')
@api_view(['GET'])
@permission_classes([IsAuthenticated])
def notification_count(request):
    user_id = request.user.id
    notifications = Notification.objects.filter(to__id = user_id)
    
    return Response(data=len(notifications), status=status.HTTP_200_OK)

@extend_schema(summary='알림페이지 DELETE')
@api_view(['DELETE'])
@permission_classes([IsAuthenticated])
def notification_delete(request, notification_id):
    notification = get_object_or_404(Notification, id = notification_id)
    notification.delete()

    return Response(status=status.HTTP_204_NO_CONTENT)