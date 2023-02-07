package com.app.myfoottrip.network.service

import com.app.myfoottrip.data.dto.Alarm
import retrofit2.Response
import retrofit2.http.GET

interface AlarmService {
    @GET("/community/notification/")
    suspend fun getAlarmList() : Response<ArrayList<Alarm>>

}