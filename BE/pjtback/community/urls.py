from django.urls import path
from . import views

urlpatterns = [
    path('board/', views.board_get),
    path('board/create/', views.board_create),
    path('board/filter/', views.board_filtered),
    path('board/detail/<int:board_id>/', views.board_detail),
    path('travel/', views.travel_get),
    path('travel/create/', views.travel_create),
    path('travel/detail/<int:travel_id>/', views.travel_detail),
    path('travel/user/<int:user_id>/',views.travel_user),
    path('board/like/<int:board_id>/', views.like),
]