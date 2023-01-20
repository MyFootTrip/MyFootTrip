from django.urls import path
from . import views

urlpatterns = [
    path('board/', views.board_get),
    path('board/create/', views.board_create),
    path('board/filter/', views.board_filtered),
    path('travel/', views.travel_get),
    path('travel/create/', views.travel_create),
    path('traveldetail/<int:travel_id>/', views.travel_detail),
    path('traveluser/<int:user_id>/',views.travel_user),
]