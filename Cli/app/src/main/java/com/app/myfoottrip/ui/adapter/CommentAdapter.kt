package com.app.myfoottrip.ui.adapter

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Comment
import com.app.myfoottrip.databinding.ListItemCommentBinding
import com.app.myfoottrip.util.TimeUtils
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CommentAdapter_마이풋트립"
class CommentAdapter(var commentList:List<Comment>,val userId : Int) : RecyclerView.Adapter<CommentAdapter.CommentHolder>(){

    inner class CommentHolder(private val binding: ListItemCommentBinding) : RecyclerView.ViewHolder(binding.root){

        fun bindInfo(comment : Comment){
            binding.apply {
                //프로필 이미지
                if (comment.profileImg.isNullOrEmpty()){
                    ivProfile.setPadding(10)
                    Glide.with(itemView).load(R.drawable.ic_my).fitCenter().into(ivProfile)
                    ivProfile .imageTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context,R.color.white))
                    cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(itemView.context,R.color.main)))
                }else {
                    Glide.with(itemView).load(comment.profileImg).centerCrop().into(ivProfile)
                    cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(itemView.context,R.color.white)))
                }

                tvNickname.text = comment.nickname
                tvContent.text = comment.content
                tvWriteDate.text = formatTimeString(TimeUtils.getFormattedLong(TimeUtils.changeDateToString(comment.writeDate)))


                if (comment.userId == userId) {
                    ivEditComment.visibility = View.VISIBLE
                }else{
                    ivEditComment.visibility = View.INVISIBLE
                }

                ivEditComment.setOnClickListener {
                    itemClickListner.onClick(it, layoutPosition,comment.commentId)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_comment, parent, false)
        return CommentHolder(ListItemCommentBinding.bind(view))
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        holder.apply{
            bindInfo(commentList[position])
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
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
}
