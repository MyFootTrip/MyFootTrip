package com.app.myfoottrip.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.databinding.ItemLoadStateBinding

class BoardPagingLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<BoardPagingLoadStateAdapter.PagingLoadStateViewHolder>() {
    inner class PagingLoadStateViewHolder(
        private val binding: ItemLoadStateBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(state: LoadState) {
            binding.lottieHome.isVisible = state is LoadState.Loading
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PagingLoadStateViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PagingLoadStateViewHolder(ItemLoadStateBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: PagingLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
}
