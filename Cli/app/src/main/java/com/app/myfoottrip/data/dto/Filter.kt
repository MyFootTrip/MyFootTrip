package com.app.myfoottrip.data.dto

data class Filter @JvmOverloads constructor(
    val themeList : ArrayList<String>, //여행 테마
    val regionList : ArrayList<String>, //여행 지역
    val periodList : ArrayList<String>, //여행 기간
    val ageList : ArrayList<String> //연령대
)
