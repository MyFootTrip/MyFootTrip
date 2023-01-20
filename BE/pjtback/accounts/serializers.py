from rest_framework import serializers
from dj_rest_auth.registration.serializers import RegisterSerializer
from dj_rest_auth.serializers import UserDetailsSerializer, JWTSerializer
from django.utils.translation import gettext_lazy as _
# 회원가입 시리얼라이저

from rest_framework import serializers
from dj_rest_auth.utils import import_callable
from dj_rest_auth.serializers import UserDetailsSerializer

from allauth.account.adapter import get_adapter
from django.core.exceptions import ValidationError as DjangoValidationError
from allauth.account.utils import setup_user_email
from django.utils.module_loading import import_string

from rest_framework.validators import UniqueValidator
from django.contrib.auth import get_user_model
from .models import User, ImageTest

from community.serializers import PlaceSerializer, BoardListSerializer, TravelSerializer


class CustomRegisterSerializer(RegisterSerializer):
    # 기본 설정 필드: username, password, email
    username = serializers.CharField(required=False)
    # 추가 설정 필드: phone_number, profile_image, naver_email, kakao_email, google_email
    # 비밀번호 해제
    password1 = serializers.CharField(write_only=True, required=False)
    password2 = serializers.CharField(write_only=True, required=False)
    # 해제 후 password 하나로 만듦
    password = serializers.CharField(write_only=True, source="password1")
    phone_number = serializers.CharField(max_length=13, required=False)
    profileImg = serializers.ImageField(use_url=True, required=False)
    naver_email = serializers.EmailField(required=False)
    kakao_email = serializers.EmailField(required=False)
    google_email = serializers.EmailField(required=False)
    nickname = serializers.CharField(min_length=1, required=True)
    age = serializers.CharField(required=False)

    # password1, password2, 검증 해제
    def validate(self, data):
        return data

    def get_cleaned_data(self):
        data = super().get_cleaned_data()
        data['profileImg'] = self.validated_data.get('profileImg', '')
        data['phone_number'] = self.validated_data.get('phone_number', '')
        data['naver'] = self.validated_data.get('naver', '')
        data['google'] = self.validated_data.get('google', '')
        data['kakao'] = self.validated_data.get('kakao', '')
        data['nickname'] = self.validated_data.get('nickname', 'Ghost')
        data['age'] = int(self.validated_data.get('age'))

        return data

    def save(self, request):
        adapter = get_adapter()
        user = adapter.new_user(request)
        print(request.FILES)
        self.cleaned_data = self.get_cleaned_data()
        if "password1" in self.cleaned_data:
            try:
                adapter.clean_password(
                    self.cleaned_data['password1'], user=user)
            except DjangoValidationError as exc:
                raise serializers.ValidationError(
                    detail=serializers.as_serializer_error(exc)
                )
        user = adapter.save_user(request, user, self, commit=False)
        user.save()
        self.custom_signup(request, user)
        setup_user_email(request, user, [])
        return user


# 유저 디테일 시리얼라이저
class CustomUserDetailSerializer(UserDetailsSerializer):
    pw = serializers.CharField(source="password", read_only=True)
    name = serializers.CharField(source="username", required=False)
    age = serializers.CharField()

    class Meta(UserDetailsSerializer.Meta):
        fields = ('email', 'pw', 'name', 'nickname',
                  'profileImg', 'age', 'kakao', 'naver', 'google')
        read_only_fields = ('email', 'pw',)


class JoinSerializer(serializers.ModelSerializer):
    
    class Meta:
        model = User
        fields = ('email', 'password', 'username', 'nickname',
                  'profileImg', 'age', 'kakao', 'naver', 'google')
        read_only_fields = ('email', 'password',)


class TokenSerializer(JWTSerializer):
    access_token = serializers.CharField()
    refresh_token = serializers.CharField()
    user = ''

    class Meta:
        fields = ('access_token', 'refresh_token',)


class CustomJWTSerializer(JWTSerializer):
    """
    Serializer for JWT authentication.
    """
    access_token = ''
    refresh_token = ''
    user = ''

    uid = serializers.IntegerField(source='user.id')
    travel = TravelSerializer(source='user.travel', many=True)
    myLikeBoard = BoardListSerializer(source='user.myLikeBoard', many=True)
    writeBoard = BoardListSerializer(source='user.writeBoard', many=True)
    token = TokenSerializer(source='*')
    join = JoinSerializer(source="user")
    totalDate = serializers.IntegerField(source='user.id')
    

    class Meta:
        fields = ('uid', 'token',  'join', 'travel',
                  'myLikeBoard', 'writeBoard', 'totalDate',)


class EmailUniqueCheckSerializer(serializers.ModelSerializer):
    email = serializers.CharField(required=True, min_length=3, max_length=30, validators=[
                                  UniqueValidator(queryset=get_user_model().objects.all())])

    class Meta:
        model = User
        fields = ('email', )


class PhoneUniqueCheckSerializer(serializers.ModelSerializer):
    phone_number = serializers.CharField(required=True, min_length=3, max_length=30, validators=[
                                         UniqueValidator(queryset=get_user_model().objects.all())])

    class Meta:
        model = User
        fields = ('phone_number', )


class UserForignSerializer(serializers.ModelSerializer):
    travel = PlaceSerializer(many=True)
    myLikeBoard = BoardListSerializer(many=True)
    writeBoard = BoardListSerializer(many=True)

    class Meta:
        model = User
        fields = ('travel', 'myLikeBoard', 'writeBoard',)

class ImageTestSerializer(serializers.ModelSerializer):

    class Meta:
        model = ImageTest
        fields = "__all__"