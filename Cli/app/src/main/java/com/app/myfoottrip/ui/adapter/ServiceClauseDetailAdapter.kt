package com.app.myfoottrip.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.data.dto.ServiceClauseText
import com.app.myfoottrip.databinding.ListItemServiceClauseBinding

private const val TAG = "ServiceClauseDetailAdapter_싸피"

class ServiceClauseDetailAdapter(val context: Context, private val list: List<ServiceClauseText>) :
    RecyclerView.Adapter<ServiceClauseDetailAdapter.ServiceClauseTextHolder>() {
    private lateinit var binding: ListItemServiceClauseBinding

    inner class ServiceClauseTextHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindInfo(data: ServiceClauseText) {
            binding.serviceClauseSubTitleTv.text = data.title
            binding.serviceClauseContentTv.text = data.content
        }
    } // End of ServiceClauseTextHolder inner class

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceClauseTextHolder {
        binding = ListItemServiceClauseBinding.inflate(LayoutInflater.from(parent.context))
        return ServiceClauseTextHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ServiceClauseTextHolder, position: Int) {
        holder.bindInfo(list[position])
    }

    override fun getItemCount() = list.size
} // End of ServiceClauseDetailAdapter class
