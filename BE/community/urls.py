from django.urls import path
from . import views

urlpatterns = [
    path('board/', views.board_get),
    path('board/create/', views.board_create),
    path('board/detail/<int:board_id>/', views.board_detail),
    path('board/like/<int:board_id>/', views.like),
    path('board/detail/<int:board_id>/comment/create/', views.comment_create),
    path('board/detail/<int:board_id>/comment/<int:comment_id>/',views.comments),
    path('board/user/',views.user_board),
    path('board/user/like/',views.user_like_board),
    path('travel/', views.travel_get),
    path('travel/create/', views.travel_create),
    path('travel/detail/<int:travel_id>/', views.travel_detail),
    path('travel/user/<int:user_id>/',views.travel_user),

    # notification 나중에 mypage app 같은 곳으로 빼던가 해야 할듯
    path('notification/', views.notification),
    path('notification/count/', views.notification_count),
    path('notification/<int:notification_id>/', views.notification_delete),
]