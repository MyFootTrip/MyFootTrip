"""pjtback URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path, include
from rest_framework_simplejwt.views import (
    TokenObtainPairView,
    TokenRefreshView,
    TokenBlacklistView,
)
from django.conf.urls import url
from rest_framework import permissions

from dj_rest_auth.registration.views import RegisterView
from dj_rest_auth.views import ( LoginView )

# django graphene
from django.views.decorators.csrf import csrf_exempt
from graphene_django.views import GraphQLView

from drf_spectacular.views import SpectacularAPIView, SpectacularSwaggerView

# 미디어파일 url 추가
from django.conf.urls.static import static
from django.conf import settings

# 스태틱파일 url 추가
from django.contrib.staticfiles.urls import staticfiles_urlpatterns

urlpatterns = [
    path('admin/', admin.site.urls),

    path('registration/', RegisterView.as_view() , name = 'registration'),
    path('login/', LoginView.as_view(), name='rest_login'),

    path('accounts/', include('accounts.urls')),

    path('token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('token/blacklist/', TokenBlacklistView.as_view(), name='token_blacklist'),
    
    path('community/', include('community.urls')),

    url('account/', include('allauth.urls')),

    # django graphene
    path("graphql/", csrf_exempt(GraphQLView.as_view(graphiql=True))),

    # SWAGGER

    path('schema/', SpectacularAPIView.as_view(), name='schema'),
    path('swagger/', SpectacularSwaggerView.as_view(url_name='schema'), name='swaggerui')
] 

# 미디어파일 url 추가
urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
urlpatterns += staticfiles_urlpatterns()
