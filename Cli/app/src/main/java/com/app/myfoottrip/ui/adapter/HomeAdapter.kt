package com.app.myfoottrip.ui.adapter

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.Application
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.bumptech.glide.Glide

private const val TAG = "HomeAdapter_마이풋트립"
class HomeAdapter(var boardList:List<Board>) : RecyclerView.Adapter<HomeAdapter.BoardHolder>(){

    inner class BoardHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val image = itemView.findViewById<ImageView>(R.id.iv_image)
        val title = itemView.findViewById<TextView>(R.id.tv_title)
        val nickname = itemView.findViewById<TextView>(R.id.tv_nickname)
        val likeCount = itemView.findViewById<TextView>(R.id.tv_like_count)
        val commentCount = itemView.findViewById<TextView>(R.id.tv_comment_count)

        fun bindInfo(board : Board){
            Glide.with(itemView)
                .load(board.imageList[0]).centerCrop()
                .into(image)

            title.text = board.title
            nickname.text = board.nickname
            likeCount.text = board.likeCount.toString()
            commentCount.text = board.commentCount.toString()

            itemView.setOnClickListener{
                itemClickListner.onClick(it, layoutPosition,board.boardId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_home, parent, false)
        return BoardHolder(view)
    }

    override fun onBindViewHolder(holder: BoardHolder, position: Int) {
        holder.apply{
            bindInfo(boardList[position])
        }
    }

    override fun getItemCount(): Int {
        return boardList.size
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
}
