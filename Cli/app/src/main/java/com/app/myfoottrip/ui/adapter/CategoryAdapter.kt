package com.app.myfoottrip.ui.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.github.nikartm.button.FitButton

class CategoryAdatper(var categoryList:List<String>) : RecyclerView.Adapter<CategoryAdatper.CategoryHolder>(){

    inner class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val categoryName = itemView.findViewById<TextView>(R.id.tv_detail)

        fun bindInfo(category : String){
            categoryName.text = category

            itemView.setOnClickListener{
                itemClickListner.onClick(it, layoutPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_category, parent, false)
        return CategoryHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.apply{
            bindInfo(categoryList[position])
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    //클릭 인터페이스 정의 사용하는 곳에서 만들어준다.
    interface ItemClickListener {
        fun onClick(view: View,  position: Int)
    }

    //클릭리스너 선언
    private lateinit var itemClickListner: ItemClickListener
    //클릭리스너 등록 매소드
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }
}
