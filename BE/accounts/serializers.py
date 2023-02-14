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

from rest_framework.validators import UniqueValidator
from django.contrib.auth import get_user_model
from .models import User, FireBase

from community.serializers import PlaceSerializer, BoardListSerializer, TravelSerializer, CommentSerializer



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
    naver_email = serializers.EmailField(required=False, allow_null = True)
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
        if self.validated_data.get('age'):
            data['age'] = int(self.validated_data.get('age'))
        else:
            data['age'] = self.validated_data.get('age')

        return data

    def save(self, request):
        print(request.data)
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
        # setup_user_email(request, user, [])
        return user


# 유저 디테일 시리얼라이저
class CustomUserDetailSerializer(UserDetailsSerializer):
    profile_image = serializers.ImageField(source='profileImg', use_url = True, required=False, allow_null = True)
    
    class Meta(UserDetailsSerializer.Meta):
        fields = ('email', 'password', 'username', 'nickname',
                  'profile_image', 'age', 'kakao', 'naver', 'google',)
        read_only_fields = ('email', 'password',)
    @staticmethod
    def validate_username(username):
        return username
    
    def update(self,instance, validated_data):
        if not validated_data.get('profileImg'):
            validated_data['profileImg']= instance.profileImg 

        super().update(instance, validated_data)
        instance.save()
        return instance

class JoinSerializer(serializers.ModelSerializer):
    profile_image = serializers.ImageField(source='profileImg', use_url = True)
    class Meta:
        model = User
        fields = ('email', 'password', 'username', 'nickname',
                  'profile_image', 'age', 'kakao', 'naver', 'google')
        read_only_fields = ('email', 'password',)


class CustomJWTSerializer(JWTSerializer):
    """
    Serializer for JWT authentication.
    """
    # access_token = ''
    # refresh_token = ''
    user = ''

    # uid = serializers.IntegerField(source='user.id')
    # travel = TravelSerializer(source='user.travel', many=True)
    # myLikeBoard = BoardListSerializer(source='user.myLikeBoard', many=True)
    # writeBoard = BoardListSerializer(source='user.writeBoard', many=True)
    # token = TokenSerializer(source='*')
    # join = JoinSerializer(source="user")
    # totalDate = serializers.IntegerField(source='user.id')
    

    class Meta:
        fields = ('access_token', 'refresh_token')


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

from rest_framework_simplejwt.serializers import TokenRefreshSerializer
from rest_framework_simplejwt.settings import api_settings
from rest_framework_simplejwt.tokens import RefreshToken
class CustomTokenRefreshSerializer(TokenRefreshSerializer):
    refresh = ""
    refresh_token = serializers.CharField()
    access_token = serializers.CharField(read_only=True)
    token_class = RefreshToken

    def validate(self, attrs):
        refresh = self.token_class(attrs["refresh_token"])

        data = {"access_token": str(refresh.access_token), "refresh_token": str(refresh)}

        if api_settings.ROTATE_REFRESH_TOKENS:
            if api_settings.BLACKLIST_AFTER_ROTATION:
                try:
                    # Attempt to blacklist the given refresh token
                    refresh.blacklist()
                except AttributeError:
                    # If blacklist app not installed, `blacklist` method will
                    # not be present
                    pass

            refresh.set_jti()
            refresh.set_exp()
            refresh.set_iat()

            data["refresh_token"] = str(refresh)

        return data

# 유저 디테일 시리얼라이저
class AllUserDetailSerializer(UserDetailsSerializer):
    uid = serializers.IntegerField(source="id", read_only=True)
    join = JoinSerializer(source="*")
    travel = TravelSerializer(many=True, read_only=True)
    myLikeBoard = BoardListSerializer(many=True, read_only=True)
    writeBoard = BoardListSerializer( many=True, read_only=True)
    commentList = CommentSerializer(many=True, read_only=True)

    class Meta(UserDetailsSerializer.Meta):
        fields = ('uid', 'join', 'travel', 'myLikeBoard', 'commentList', 'writeBoard')
        read_only_fields = ()

# 블랙리스트 시리얼라이저
class CustomTokenBlacklistSerializer(serializers.Serializer):
    refresh_token = serializers.CharField()
    refresh_token_class = RefreshToken

    def validate(self, attrs):
        refresh = self.refresh_token_class(attrs["refresh_token"])
        try:
            refresh.blacklist()
        except AttributeError:
            pass
        return True
    def post(self):
        return 'ok'



# firebase 토큰 시리얼라이저
class FirebaseSerializer(UserDetailsSerializer):
    firebaseToken = serializers.CharField(source = 'fcmToken', required= True)

    class Meta:
        model=FireBase
        fields = ('__all__')