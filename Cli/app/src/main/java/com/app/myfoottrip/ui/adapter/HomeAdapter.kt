package com.app.myfoottrip.ui.adapter

import android.content.res.ColorStateList
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.Application
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Image
import com.app.myfoottrip.databinding.DialogPlaceBottomBinding
import com.app.myfoottrip.databinding.ListItemHomeBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

private const val TAG = "HomeAdapter_마이풋트립"
class HomeAdapter(var boardList:List<Board>) : RecyclerView.Adapter<HomeAdapter.BoardHolder>(){

    inner class BoardHolder(val binding: ListItemHomeBinding) : RecyclerView.ViewHolder(binding.root){
        
        fun bindInfo(board : Board){
            binding.apply {
                //게시물 사진
                Glide.with(itemView)
                    .load(board.imageList[0]).thumbnail(Glide.with(itemView).load(R.drawable.loading_image).centerCrop()).centerCrop()
                    .into(ivImage)

                //이미지 적용 (Glide 라이브러리 사용)
                val options = RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                
                //프로필 이미지
                if (board.profileImg.isNullOrEmpty()){
                    ivProfile.setPadding(10)
                    Glide.with(itemView).load(R.drawable.ic_my).fitCenter().into(ivProfile)
                    ivProfile.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context,R.color.white))
                    cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(itemView.context,R.color.main)))
                }else {
                    Glide.with(itemView).load(board.profileImg).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().into(ivProfile)
                    cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(itemView.context,R.color.white)))
                }

                tvTheme.text = "#${board.theme}"
                tvLocation.text = convertToString(board.travel!!.location!! as ArrayList<String>) //여행 지역
                tvTitle.text = board.title
                tvNickname.text = board.nickname
                tvLikeCount.text = board.likeList.size.toString()
                tvCommentCount.text = board.commentList.size.toString()
            }

            itemView.setOnClickListener{
                itemClickListner.onClick(it, layoutPosition,board.boardId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardHolder {
        val view = LayoutInflater.from(parent.context) //recyclerview에 각 아이템에 들어갈 layout 인플레이트
                .inflate(R.layout.list_item_home, parent, false)
        return BoardHolder(ListItemHomeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: BoardHolder, position: Int) {
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
