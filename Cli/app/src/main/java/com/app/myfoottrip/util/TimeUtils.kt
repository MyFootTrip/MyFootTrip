package com.app.myfoottrip.util

import android.content.Context
import android.widget.Toast
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private val SDF1 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")


    //시간차 구하기
    fun getDiffTime(time: Long): String {
        val SDformat = SimpleDateFormat("HH:mm")
        val dateFormat = SimpleDateFormat("MM.dd HH:mm")
        val now = System.currentTimeMillis()
        val diff = now - time
        val date: String = if (diff >= 24 * 60 * 60 * 1000) { //하루가 넘을 때
            dateFormat.format(time).toString()
        } else if (diff >= 60 * 60 * 1000) { //한 시간 넘을 때
            SDformat.format(time).toString()
        } else { //한 시간 전
            "${diff / (60 * 1000)} 분전"
        }
        return date
    }

    fun getDday(time: Long): String {
        val now = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        val time_day = time / (24 * 60 * 60 * 1000)
        val day = (time_day - now).toInt()
        return if (day < 0) {
            "기한지남"
        } else if (day == 0) {
            "D-day"
        } else {
            "D-$day"
        }
    }

    private fun getDateString(cal: Calendar): String {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val date = cal.get(Calendar.DATE)
        return "${String.format("%04d", year)}.${
            String.format(
                "%02d",
                month + 1
            )
        }.${String.format("%02d", date)}" //버튼 text 변경
    }

    fun getDateString(time: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        return getDateString(cal)
    }

    fun getDateString(date: Date): String {
        val dateFormatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        return dateFormatter.format(date)
    }

    fun getDateTimeString(time: Long): String {
        return SDF1.format(time)
    }

    fun getFormattedString(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
        dateFormat.timeZone = TimeZone.getTimeZone("Seoul/Asia")
        return dateFormat.format(date)
    }

    private fun getNowTime(): Long {
        return System.currentTimeMillis()
    } // End of nowTimeAsLong

    fun getNowTimeAsLongType(): Long {
        val date = Date(getNowTimeAsLongType())
        val sdf = SDF1
        val tmp = sdf.format(date)

        return sdf.parse(tmp).time
    } // End of getNowTimeAsDateType

}