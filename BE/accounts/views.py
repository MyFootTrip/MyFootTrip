from rest_framework_simplejwt.tokens import RefreshToken
from django.shortcuts import render
from django.contrib.auth import get_user_model
from django.shortcuts import render, redirect, get_object_or_404, get_list_or_404
from django.contrib.auth import login
from drf_spectacular.utils import extend_schema, OpenApiParameter, OpenApiTypes, OpenApiExample, inline_serializer
from django.http import JsonResponse, HttpResponse
from rest_framework import status
from rest_framework.response import Response
import requests
import json
from rest_framework.decorators import api_view
from django.views.decorators.csrf import csrf_exempt
from rest_framework import serializers

from .serializers import EmailUniqueCheckSerializer, PhoneUniqueCheckSerializer, CustomUserDetailSerializer, JoinSerializer, AllUserDetailSerializer, FirebaseSerializer, CustomTokenBlacklistSerializer, CustomRegisterSerializer

from .models import EmailValidateModel, FireBase

from rest_framework_simplejwt.views import TokenBlacklistView


# @extend_schema(tags=['registration'], request=PhoneUniqueCheckSerializer, responses=bool, summary='폰 넘버 중복 체크')
# @api_view(['POST'])
# @csrf_exempt
# def filtering_phone(request):
#     serializer = PhoneUniqueCheckSerializer(data=request.data)
#     if serializer.is_valid():
#         return HttpResponse(False)
#     else:
#         return HttpResponse(True)

# 이메일 보내기
from django.core.mail import EmailMessage
# 랜덤 코드 생성
from django.utils.crypto import get_random_string

@extend_schema(tags=['registration'], request=EmailUniqueCheckSerializer, responses=bool, summary='email 중복 체크')
@api_view(['POST'])
@csrf_exempt
def filtering_email(request):
    # emailobj = {"email": request.data['']}
    serializer = EmailUniqueCheckSerializer(data=request.data)
    if serializer.is_valid():
        # 이메일인증 오브젝트 생성
        vcode = get_random_string(length=6, allowed_chars='1234567890')
        if EmailValidateModel.objects.filter(email=request.data['email']):
            EmailValidateModel.objects.filter(email=request.data['email']).delete()
        EmailValidateModel.objects.create(email=request.data['email'], validateNumber=vcode)
        email = EmailMessage(
            '마이 풋 트립 계정 인증', #이메일 제목
            f'인증 번호 : {vcode}', #내용
            to=[request.data['email'],], #받는 이메일
        )
        email.send()
        return HttpResponse(False)
    else:
        return HttpResponse(True)

@api_view(['POST'])
@csrf_exempt
def validate_email(request):
    try:
        valid = EmailValidateModel.objects.get(email = request.data['email'])
    except:
        return HttpResponse(False)
    if valid.validateNumber == request.data['validateNumber']:
        valid.delete()
        return HttpResponse(True)
    else:
        return HttpResponse(False)

from dj_rest_auth.registration.views import RegisterView
# 소셜 로그인 시 유저 정보 조회 후 토큰 발급
@extend_schema(tags=['login'], request=inline_serializer(
    name="InlineFormSerializer",
    fields={
        "token": serializers.CharField(),
    },
), responses={"multipart/form-data": {
    "type": "object",
    "properties": {
        "token": {"type": "object", "properties": {"accesstoken": {"type": "string"}, "refreshtoken": {"type": "string"}}},
        "user": {"type": "object"}},
}}, summary='소셜 로그인 및 토큰 발급')
@api_view(['GET', 'POST'])
@csrf_exempt
def social_login(request, social_page):
    # TEST CODE
    # https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=FL_dHs6b8BOH36DPExe3&redirect_uri=http://127.0.0.1:8000/accounts/social_login/naver/
    # print(request)
    # print(request.GET.get('code'))
    # usercode = request.GET.get('code')
    # userkey = requests.get(url=f'https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&client_id=FL_dHs6b8BOH36DPExe3&client_secret=p_IuEJYQOc&code={usercode}&state=9kgsGTfH4j7IyAkg', )
    # print(json.loads(userkey.content.decode('utf-8')))
    # usertoken = json.loads(userkey.content.decode('utf-8')).get("access_token")
    # print(usertoken)
    # userdata = requests.get(url="https://openapi.naver.com/v1/nid/me", headers={"Authorization": f"Bearer {usertoken}"})
    # print(json.loads(userdata.content.decode('utf-8')))
    # useremail = json.loads(userdata.content.decode('utf-8')).get('response').get('email')
    # print(useremail)

    # 네이버의 경우
    if social_page == "naver":
        usertoken = request.data["token"]
        userdata = requests.get(url="https://openapi.naver.com/v1/nid/me",
                                headers={"Authorization": f"Bearer {usertoken}"})

        useremail = json.loads(userdata.content.decode(
            'utf-8')).get('response').get('email')
    # 카카오의 경우
    if social_page == "kakao":
        usertoken = request.data["token"]
        userdata = requests.get(url="https://kapi.kakao.com/v2/user/me",
                                headers={"Authorization": f"Bearer {usertoken}"}).json()
        useremail = userdata["kakao_account"]["email"]

        
    # 구글의 경우
    elif social_page == "google":
        usertoken = request.POST.get("token")
        userdata = requests.get(url="www.googleapis.com/drive/v2/files",
                                headers={"Authorization": f"Bearer {usertoken}"})
    # else:
    #     return Response(status=status.HTTP_404_NOT_FOUND)

    # 유저모델 호출
    User = get_user_model()
    try:
        user = User.objects.get(email=useremail)
    except:
        vcode = get_random_string(length=8)
        # serializer = CustomRegisterSerializer( data={'email': f'{useremail}', 'password': '1q2w3e4r!@#$%', 'nickname': f'{social_page}_{vcode}', 'age': 10})
        # if serializer.is_valid(raise_exception=True):
        #     serializer.save(request={'data': {'email': f'{useremail}', 'password': '1q2w3e4r!@#$%', 'nickname': f'{social_page}_{vcode}', 'age': 10}})
        # user = User.objects.get(email=useremail)
        # class ReUser():
        #     def __init__(self, data):
        #         self.data = data
        #     def get_data(self):
        #         return self.data
        # ReUser.data = {'email': f'{useremail}', 'password': '1q2w3e4r!@#$%', 'nickname': f'{social_page}_{vcode}', 'age': 10}
        # RegisterView.create(self=RegisterView, request=ReUser)
        # 임시로 때려박은 유저...
        user = User.objects.create(email=f'{useremail}', password='1q2w3e4r!@#$%', nickname=f'{social_page}_{vcode}', age = 10)
        
    token = get_tokens_for_user(user)
    print(token)
    return JsonResponse(token, status=status.HTTP_200_OK)

# 토큰 생성 함수


def get_tokens_for_user(user):
    refresh = RefreshToken.for_user(user)

    return {
        'refresh_token': str(refresh),
        'access_token': str(refresh.access_token),
    }

from rest_framework.decorators import permission_classes
from rest_framework.permissions import IsAuthenticated

@csrf_exempt
@api_view(['GET'])
@permission_classes([IsAuthenticated])
def join_views(request):
    user = request.user
    serializer = AllUserDetailSerializer(user, context={"request": request})
    return Response(serializer.data, status=status.HTTP_200_OK)


@csrf_exempt
@api_view(['POST', 'PUT'])
@permission_classes([IsAuthenticated])
def f_token_save_views(request):
    if request.method == 'POST':
        if len(FireBase.objects.filter(fcmToken = request.data['firebaseToken'], user=request.user)) == 0:
            serializer = FirebaseSerializer(data = request.data)

            if serializer.is_valid(raise_exception=True):
                serializer.save(user=request.user)
                return Response(data=serializer.data['firebaseToken'], status=status.HTTP_200_OK)
        else:
            return Response(data=request.data['firebaseToken'], status=status.HTTP_200_OK)
    elif request.method == 'PUT':
        FireBase.objects.filter(fcmToken = request.data['firebaseToken']).delete()
        return Response(data=request.data['firebaseToken'], status=status.HTTP_200_OK)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def change_email(request):
    user = request.user
    user.email = request.data['email']
    user.save()

    return Response(status=status.HTTP_200_OK)

@api_view(['POST'])
def test(request):
    print(request.data)
    print(request.FILES)
    return Response(status=status.HTTP_200_OK)
