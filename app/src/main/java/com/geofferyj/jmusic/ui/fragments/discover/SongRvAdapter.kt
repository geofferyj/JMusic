package com.geofferyj.jmusic.ui.fragments.discover

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.geofferyj.jmusic.data.models.Song
import com.geofferyj.jmusic.databinding.SongItemBinding

class SongRvAdapter : RecyclerView.Adapter<SongRvAdapter.SongVH>() {
    inner class SongVH(private val binding: SongItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.song = song
            binding.root.setOnClickListener {
                onItemClickListener?.let {
                    it(song)
                }
            }
        }
    }

    private var onItemClickListener: ((Song) -> Unit)? = null

    fun setItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }

    private val differCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Song, newItem: Song) = oldItem == newItem
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = SongItemBinding.inflate(layoutInflater, parent, false)
        return SongVH(binding)
    }

    override fun onBindViewHolder(holder: SongVH, position: Int) {

        val song = differ.currentList[position]

        song?.let {
            holder.bind(song)
        }
    }

    override fun getItemCount() = differ.currentList.size
}