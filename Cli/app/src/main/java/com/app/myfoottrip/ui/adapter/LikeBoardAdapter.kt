package com.app.myfoottrip.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.databinding.ListItemHomeBinding
import com.app.myfoottrip.databinding.ListItemMypageLikeBinding
import com.bumptech.glide.Glide

private const val TAG = "LikeBoardAdapter_마이풋트립"
class LikeBoardAdapter(var boardList:List<Board>) : RecyclerView.Adapter<LikeBoardAdapter.LikeBoardHolder>(){

    inner class LikeBoardHolder(val binding: ListItemMypageLikeBinding) : RecyclerView.ViewHolder(binding.root){
        
        fun bindInfo(board : Board){
            binding.apply {

                //게시물 사진
                Glide.with(itemView)
                    .load(board.imageList[0]).thumbnail(Glide.with(itemView).load(R.drawable.loading_image).centerCrop()).centerCrop()
                    .into(ivImage)

                tvTheme.text = "#${board.theme}"
                tvLocation.text = convertToString(board.travel!!.location!! as ArrayList<String>) //여행 지역
                tvTitle.text = board.title
                tvLikeCount.text = board.likeList.size.toString()
            }

            itemView.setOnClickListener{
                itemClickListner.onClick(it, layoutPosition,board.boardId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeBoardHolder {
        val view = LayoutInflater.from(parent.context) //recyclerview에 각 아이템에 들어갈 layout 인플레이트
                .inflate(R.layout.list_item_mypage_like, parent, false)
        return LikeBoardHolder(ListItemMypageLikeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: LikeBoardHolder, position: Int) {
        holder.apply{
            bindInfo(boardList[position])
        }
    }

    override fun getItemCount(): Int {
        return boardList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    //클릭 인터페이스 정의 사용하는 곳에서 만들어준다.
    interface ItemClickListener {
        fun onClick(view: View,  position: Int, boardId : Int)
    }

    //클릭리스너 선언
    private lateinit var itemClickListner: ItemClickListener
    //클릭리스너 등록 매소드
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }

    //테마 및 지역 #붙인 스트링으로 만들기
    private fun convertToString(stringList : ArrayList<String>) : String{
        val sb = StringBuilder()
        for (str in stringList){
            sb.append("#${str} ")
        }
        return sb.toString()
    }
}
