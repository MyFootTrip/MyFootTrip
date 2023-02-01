package com.app.myfoottrip.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.databinding.ListItmeTravelEditSaveBinding

private const val TAG = "TravelEditSaveItemAdapt_싸피"

class TravelEditSaveItemAdapter(val context: Context, private val placeList: List<Place>) :
    RecyclerView.Adapter<TravelEditSaveItemAdapter.TravelEditSaveItemHolder>() {
    private lateinit var binding: ListItmeTravelEditSaveBinding

    //클릭리스너 선언
    private lateinit var itemClickListner: ItemClickListener

    inner class TravelEditSaveItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindInfo(data: Place, index: Int) {
            binding.travelNumberTv.text = "${index + 1}"
            binding.travelItemAddressTv.text = data.address.toString()
            binding.travelItemDateTv.text = data.memo.toString()
        }
    } // End of inner class

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelEditSaveItemHolder {
        binding = ListItmeTravelEditSaveBinding.inflate(LayoutInflater.from(parent.context))
        return TravelEditSaveItemHolder(binding.root)
    } // End of onCreateViewHolder

    override fun onBindViewHolder(holder: TravelEditSaveItemHolder, position: Int) {
        holder.bindInfo(placeList[position], position)
    } // End of onCreateViewHolder

    override fun getItemCount(): Int = placeList.size

    interface ItemClickListener {
        fun onEditButtonClick(position: Int, placeData: Place) // 수정 버튼을 클릭했을 때 이벤트
    } // End of ItemClickListener interface

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    } // End of setItemClickListener
} // End of TravelEditSaveItemAdapter
