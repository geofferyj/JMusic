package com.geofferyj.jmusic.ui.fragments.feed.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.geofferyj.jmusic.databinding.AccountViewItemBinding

class SuggestedAccountsAdapter: RecyclerView.Adapter<SuggestedAccountsAdapter.SAVH>() {

    inner class SAVH(val binding: AccountViewItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(account: Int?) {
            binding.root.setOnClickListener {
                onClickListener?.invoke("root")
            }

            binding.followBtn.setOnClickListener {
                onClickListener?.invoke("button")
            }
        }
    }

    private var onClickListener: ((type: String)->Unit)? = null

    fun setOnItemClickListener(listener: (type: String)->Unit){
        onClickListener = listener
    }
    private val differCallback = object: DiffUtil.ItemCallback<Int>(){
        override fun areItemsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
        override fun areContentsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
    }

    val differ = AsyncListDiffer(this, differCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SAVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AccountViewItemBinding.inflate(inflater, parent, false)
        return SAVH(binding)
    }

    override fun onBindViewHolder(holder: SAVH, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount() = differ.currentList.size


    fun submitList(list: List<Int>){
        differ.submitList(list)
    }

    fun updateList(list: List<Int>){
        val oldList = differ.currentList
        val newList = oldList + list
        differ.submitList(newList)
    }
    companion object {
        private const val TAG = "SuggestedAccountsAdapter"
    }
}