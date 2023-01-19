package com.app.myfoottrip.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Comment
import com.app.myfoottrip.util.TimeUtils

private const val TAG = "CommentAdapter_마이풋트립"
class CommentAdapter(var commentList:List<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentHolder>(){

    inner class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val profileImg = itemView.findViewById<ImageView>(R.id.iv_profile)
        val nickname = itemView.findViewById<TextView>(R.id.tv_nickname)
        val content = itemView.findViewById<TextView>(R.id.tv_content)
        val writeDate = itemView.findViewById<TextView>(R.id.tv_write_date)
        val editBtn = itemView.findViewById<ImageView>(R.id.iv_edit_comment)

        fun bindInfo(comment : Comment){

//            Glide.with(itemView)
//                .load("${Application.IMG_URL}"+board.profileImgList[0]).centerCrop()
//                .into(profileImg)

            nickname.text = comment.nickname
            content.text = comment.content
            writeDate.text = TimeUtils.getFormattedString(comment.writeDate)

            editBtn.setOnClickListener {
                itemClickListner.onClick(it, layoutPosition,comment.commentId)
            }

//            itemView.setOnClickListener{
//                itemClickListner.onClick(it, layoutPosition,comment.commentId)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_comment, parent, false)
        return CommentHolder(view)
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        holder.apply{
            bindInfo(commentList[position])
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    //클릭 인터페이스 정의 사용하는 곳에서 만들어준다.
    interface ItemClickListener {
        fun onClick(view: View,  position: Int, commentId : Int)
    }

    //클릭리스너 선언
    private lateinit var itemClickListner: ItemClickListener
    //클릭리스너 등록 매소드
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }
}
