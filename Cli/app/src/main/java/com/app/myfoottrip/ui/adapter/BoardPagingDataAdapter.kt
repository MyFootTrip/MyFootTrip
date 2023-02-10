package com.app.myfoottrip.ui.adapter

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.databinding.ListItemHomeBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

private const val TAG = "BoardPagingDataAdapter_마이풋트립"
class BoardPagingDataAdapter: PagingDataAdapter<Board,BoardPagingDataAdapter.BoardViewHolder>(
    IMAGE_COMPARATOR) {
    inner class BoardViewHolder(private val binding : ListItemHomeBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(board : Board){
            Log.d("tst5", "bind: ${board.boardId} 바인드됨")
            binding.apply {
                if (board.imageList.isNullOrEmpty()){
                    Glide.with(itemView)
                        .load(R.drawable.default_image).thumbnail(Glide.with(itemView).load(R.drawable.loading_image).centerCrop()).centerCrop()
                        .into(ivImage)
                }else{
                    Glide.with(itemView)
                        .load(board.imageList[0]).thumbnail(Glide.with(itemView).load(R.drawable.loading_image).centerCrop()).centerCrop()
                        .into(ivImage)
                }


                //이미지 적용 (Glide 라이브러리 사용)
                val options = RequestOptions().skipMemoryCache(true).diskCacheStrategy(
                    DiskCacheStrategy.NONE)

                //프로필 이미지
                if (board.profileImg.isNullOrEmpty()){
                    ivProfile.setPadding(10)
                    Glide.with(itemView).load(R.drawable.ic_my).fitCenter().into(ivProfile)
                    ivProfile.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context,
                            R.color.white))
                    cvProfileLayout.setCardBackgroundColor(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(itemView.context,
                                R.color.main)))
                }else {
                    Glide.with(itemView).load(board.profileImg).centerCrop().into(ivProfile)
                    cvProfileLayout.setCardBackgroundColor(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(itemView.context,
                                R.color.white)))
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

    //클릭 인터페이스 정의 사용하는 곳에서 만들어준다.
    interface ItemClickListener {
        fun onClick(view: View, position: Int, boardId : Int)
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

    // 어떤 xml 으로 뷰 홀더를 생성할지 지정
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val binding = ListItemHomeBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BoardViewHolder(binding)
    }

    // 뷰 홀더에 데이터 바인딩
    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val currentItem = getItem(position)

        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }

    companion object {
        private val IMAGE_COMPARATOR = object : DiffUtil.ItemCallback<Board>() {
            override fun areItemsTheSame(oldItem: Board, newItem: Board) =
                oldItem.boardId == newItem.boardId

            override fun areContentsTheSame(oldItem: Board, newItem: Board) =
                oldItem == newItem
        }
    }
}

