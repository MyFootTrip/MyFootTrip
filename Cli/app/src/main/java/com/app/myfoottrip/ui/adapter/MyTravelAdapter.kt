package com.app.myfoottrip.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.databinding.ListItemMyTravelBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

// 0 : 여정 선택, 1: 여정 보기
private const val TAG = "MyTravelAdapter_마이풋트립"

class MyTravelAdapter() : RecyclerView.Adapter<MyTravelAdapter.MyTravelHolder>() {
    private var travelList: List<Travel> = emptyList()

    //클릭리스너 선언
    private lateinit var itemClickListner: ItemClickListener

    fun setList(list: ArrayList<Travel>) {
        travelList = list
    }

    inner class MyTravelHolder(private val binding: ListItemMyTravelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindInfo(position: Int, travelDto: Travel) {

            val startDateFormat = SimpleDateFormat("yyyy-MM.dd", Locale("ko", "KR"))
            val endDateFormat = SimpleDateFormat("MM.dd", Locale("ko", "KR"))
            val startDateString = startDateFormat.format(travelList[position].startDate!!)
            val endDateString = endDateFormat.format(travelList[position].endDate!!)
            binding.tvTravelDate.text = "$startDateString - $endDateString"

            binding.apply {

                if (travelDto.placeList?.get(0)?.placeImgList!!.isNotEmpty() && travelDto.placeList?.get(0)?.placeImgList!!.size == 0) {
                    Glide.with(itemView)
                        .load(R.drawable.place_default_img)
                        .centerCrop()
                        .into(ivImage)
                } else {
                    Glide.with(itemView)
                        .load(travelDto.placeList?.get(0)?.placeImgList!![0])
                        .centerCrop()
                        .into(ivImage)
                }

                tvTravelContent.text = travelDto.location!!.joinToString(", ")

                chipTravelDelete.setOnClickListener {
                    itemClickListner.onDeleteChipClick(position, travelDto)
                }
            }
        }
    } // End of MyTravelHolder inner class

    interface ItemClickListener {
        fun onAllClick(position: Int, travelDto: Travel) //전체 클릭한 경우
        fun onChipClick(type: Int, position: Int, travelDto: Travel) //chip만 클릭한 경우
        fun onDeleteChipClick(position: Int, travelDto: Travel) // 삭제 버튼을 클릭했을 때,
    } // End of ItemClickListener

    //클릭리스너 등록 매소드
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyTravelAdapter.MyTravelHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_my_travel, parent, false)
        return MyTravelHolder(ListItemMyTravelBinding.bind(view))
    }

    override fun onBindViewHolder(holder: MyTravelHolder, position: Int) {
        holder.bindInfo(position, travelList[position])
    }

    override fun getItemCount(): Int = travelList.size


} // End of MyTravelAdapter class
