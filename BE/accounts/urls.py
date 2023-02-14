from django.urls import path
from . import views
from dj_rest_auth.views import (UserDetailsView, PasswordChangeView)
from . import serializers

urlpatterns = [
    # path('phonecheck/', views.filtering_phone),
    path('jwtdetail/', views.join_views),
    path('emailcheck/', views.filtering_email),
    path('emailvalidate/', views.validate_email),
    path('social_login/<str:social_page>/', views.social_login),
    path('userdetail/', UserDetailsView.as_view()),
    path('firebase_save/', views.f_token_save_views),
    path('changeEmail/',views.change_email ),
    path('changePassword/', PasswordChangeView.as_view()),
    path('testing/', views.test)
]
