package com.app.myfoottrip.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Alarm
import com.app.myfoottrip.databinding.ListItemAlarmBinding
import com.app.myfoottrip.util.TimeUtils
import com.bumptech.glide.Glide

// 0 : 여정 선택, 1: 여정 보기
private const val TAG = "AlarmAdapter_마이풋트립"

class AlarmAdapter(var alarmList:ArrayList<Alarm>) : RecyclerView.Adapter<AlarmAdapter.AlarmHolder>() {

    //클릭리스너 선언
    private lateinit var itemClickListner: ItemClickListener

    inner class AlarmHolder(private val binding: ListItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindInfo(position: Int, alarm: Alarm) {

            binding.tvAlarmDate.text = formatTimeString(TimeUtils.getFormattedLong(TimeUtils.changeDateToString(alarm.createDate)))

            binding.apply {
                //프로필 이미지
                if (alarm.profileImg.isNullOrEmpty()){
                    ivProfile.setPadding(30)
                    Glide.with(itemView).load(R.drawable.ic_my).fitCenter().into(ivProfile)
                    ivProfile.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context,R.color.white))
                    cvImage.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(itemView.context,R.color.main)))
                }else {
                    Glide.with(itemView).load(alarm.profileImg).thumbnail(Glide.with(itemView).load(R.drawable.loading_image).centerCrop()).centerCrop().into(ivProfile)
                    cvImage.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(itemView.context,R.color.white)))
                }

                tvAlarmContent.text = alarm.message

                ivDelete.setOnClickListener {
                    itemClickListner.onDeleteAlarm(position, alarm)
                }
            }
        }
    } // End of AlarmHolder inner class

    interface ItemClickListener {
        fun onDeleteAlarm(position: Int, alarm: Alarm) // 삭제 버튼을 클릭했을 때,
    } // End of ItemClickListener

    //클릭리스너 등록 매소드
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmAdapter.AlarmHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_alarm, parent, false)
        return AlarmHolder(ListItemAlarmBinding.bind(view))
    }

    override fun onBindViewHolder(holder: AlarmHolder, position: Int) {
        holder.bindInfo(position, alarmList[position])
    }

    override fun getItemCount(): Int = alarmList.size

    fun formatTimeString(regTime: Long): String? {
        val curTime = System.currentTimeMillis()
        var diffTime = (curTime - regTime) / 1000
        var msg: String? = null
        if (diffTime < SEC) {
            msg = "방금 전"
        } else if (SEC.let { diffTime /= it; diffTime } < MIN) {
            msg = diffTime.toString() + "분 전"
        } else if (MIN.let { diffTime /= it; diffTime } < HOUR) {
            msg = diffTime.toString() + "시간 전"
        } else if (HOUR.let { diffTime /= it; diffTime } < DAY) {
            msg = diffTime.toString() + "일 전"
        } else if (DAY.let { diffTime /= it; diffTime } < MONTH) {
            msg = diffTime.toString() + "달 전"
        } else {
            msg = diffTime.toString() + "년 전"
        }
        return msg
    }

    companion object{
        const val SEC = 60
        const val MIN = 60
        const val HOUR = 24
        const val DAY = 30
        const val MONTH = 12
    }

} // End of AlarmAdapter class
