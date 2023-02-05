package com.app.myfoottrip.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.databinding.ListItemTravelBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

// 0 : 여정 선택, 1: 여정 보기
private const val TAG = "TravelAdapter_싸피"

class TravelAdapter(
    private val type: Int = 1
) : RecyclerView.Adapter<TravelAdapter.TravelHolder>() {
    private var selected: Int = -1
    private var travelList: List<Travel> = emptyList()

    //클릭리스너 선언
    private lateinit var itemClickListner: ItemClickListener

    fun getSelected(): Int {
        return selected
    }

    fun setSelected(position: Int) {
        selected = position
    }

    fun setList(list: ArrayList<Travel>) {
        travelList = list
    }

    inner class TravelHolder(private val binding: ListItemTravelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindInfo(position: Int, travelDto: Travel) {

            val startDateFormat = SimpleDateFormat("yyyy-MM.dd", Locale("ko", "KR"))
            val endDateFormat = SimpleDateFormat("MM.dd", Locale("ko", "KR"))

            Log.d(TAG, "travelList: $travelList")

            val startDateString = startDateFormat.format(travelList[position].startDate!!)
            val endDateString = endDateFormat.format(travelList[position].endDate!!)
            binding.tvTravelDate.text = "$startDateString - $endDateString"

            binding.apply {
                if (type == 1) { //여정 선택
                    chipTravelSelect.text = "삭제"
                } else { //여정 삭제
                    chipTravelSelect.text = "선택"
                }

                chipTravelSelect.isChecked = (selected == position) //선택은 하나만 하도록

                if (travelList.isNotEmpty() && travelList[0].placeList?.get(0)?.placeImgList?.size == 0) {
                    Glide.with(itemView)
                        .load(R.drawable.place_default_img)
                        .centerCrop()
                        .into(ivTravel)
                } else {
                    Glide.with(itemView)
                        .load(travelList[0].placeList?.get(0)?.placeImgList?.get(0))
                        .centerCrop()
                        .into(ivTravel)
                }

                tvTravelName.text = travelDto.location!!.joinToString(", ")

                clTravelItem.setOnClickListener {
                    itemClickListner.onAllClick(position, travelDto)
                }
                chipTravelSelect.setOnClickListener {
                    itemClickListner.onChipClick(type, position, travelDto)
                }
            }
        }
    } // End of TravelHolder inner class

    interface ItemClickListener {
        fun onAllClick(position: Int, travelDto: Travel) //전체 클릭한 경우
        fun onChipClick(type: Int, position: Int, travelDto: Travel) //chip만 클릭한 경우
    } // End of ItemClickListener

    //클릭리스너 등록 매소드
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelAdapter.TravelHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_travel, parent, false)
        return TravelHolder(ListItemTravelBinding.bind(view))
    }

    override fun onBindViewHolder(holder: TravelHolder, position: Int) {
        holder.bindInfo(position, travelList[position])
    }

    override fun getItemCount(): Int = travelList.size
} // End of TravelAdapter class
