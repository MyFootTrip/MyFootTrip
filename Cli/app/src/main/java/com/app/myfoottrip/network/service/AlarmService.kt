package com.app.myfoottrip.network.service

import com.app.myfoottrip.data.dto.Alarm
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface AlarmService {
    @GET("/community/notification/")
    suspend fun getAlarmList() : Response<ArrayList<Alarm>>

    //알림 메세지 삭제
    @DELETE("/community/notification/{notificationId}/")
    suspend fun deleteAlarm(@Path("notificationId") notificationId:Int): Response<Void>
}