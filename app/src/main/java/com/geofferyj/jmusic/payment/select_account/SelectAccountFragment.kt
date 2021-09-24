package com.geofferyj.jmusic.payment.select_account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.geofferyj.jmusic.data.models.AccountDetails
import com.geofferyj.jmusic.databinding.FragmentSelectAccountBinding

class SelectAccountFragment : Fragment() {

    private var _binding: FragmentSelectAccountBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSelectAccountBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accounts = listOf(
            AccountDetails("Sterling Bank", "Fulfil Michael", "0067179278"),
            AccountDetails("Wema Bank", "", "7810325437"),
            AccountDetails("Kuda Bank", "Otoabasi Trinity", "2007991473"),
            AccountDetails("UBA", "Sylvester Ruth", "2070224764"),
            AccountDetails("First Bank", "Anthony Idem", "3110894491"),
            AccountDetails("GT Bank", "Aqua Clarkson Emmanuel", "0175972046"),
            AccountDetails("ZenithBank", "Godstime Akedebi", "2270036570"),
            AccountDetails("Union Bank", "Ndifreke Udobong", "0097854693"),
            AccountDetails("FCMB", "Anthony Idem", "3248967011"),
            AccountDetails("FCMB", "Geoffery Joseph", "2760367019"),
            AccountDetails("Access Bank", "Geoffery Joseph", "0729894955"),
        ).sortedBy {
            it.bankName
        }
        val search = binding.textInputLayout.editText

        val rvAdapter = AccountRVAdapter().apply {
            setOnItemClickListener {
                val action = SelectAccountFragmentDirections.actionSelectAccountFragmentToSubmitProofFragment(it)
                findNavController().navigate(action)
            }
        }

        rvAdapter.differ.submitList(accounts)
        binding.recyclerView.adapter = rvAdapter

        search?.doOnTextChanged { text, start, before, count ->
            val newList = accounts.filter {
                val bankName = it.bankName.lowercase()
                bankName.contains(text.toString().lowercase())
            }

            rvAdapter.differ.submitList(newList)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SelectAccountFragment"
    }
}