package com.geofferyj.jmusic.payment.select_account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.geofferyj.jmusic.data.models.AccountDetails
import com.geofferyj.jmusic.databinding.AccountItemBinding

class AccountRVAdapter : RecyclerView.Adapter<AccountRVAdapter.VH>() {

    inner class VH(val binding: AccountItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AccountDetails) {
            binding.bankName.text = "Bank Name: ${item.bankName}"
            binding.accountName.text = "Account Name: ${item.accountName}"
            binding.accountNumber.text = "Account Number: ${item.accountNumber}"

            binding.root.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        }
    }

    private val diffCallback = object: DiffUtil.ItemCallback<AccountDetails>(){
        override fun areItemsTheSame(
            oldItem: AccountDetails,
            newItem: AccountDetails
        ): Boolean = oldItem == newItem


        override fun areContentsTheSame(
            oldItem: AccountDetails,
            newItem: AccountDetails
        ): Boolean = oldItem == newItem

    }

    val differ = AsyncListDiffer(this, diffCallback)

    private var onItemClickListener: ((item: AccountDetails) -> Unit)? = null

    fun setOnItemClickListener(listener: (item: AccountDetails) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AccountItemBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = differ.currentList[position]
        holder.bind(item)

    }

    override fun getItemCount(): Int = differ.currentList.size

    companion object {
        private const val TAG = "CoinAddressesRV"
    }
}