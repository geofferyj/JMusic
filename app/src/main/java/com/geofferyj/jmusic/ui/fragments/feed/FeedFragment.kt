package com.geofferyj.jmusic.ui.fragments.feed

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.geofferyj.jmusic.databinding.FragmentFeedBinding
import com.geofferyj.jmusic.ui.fragments.feed.adapters.SuggestedAccountsAdapter

class FeedFragment : Fragment() {

    private  val feedViewModel: FeedViewModel by viewModels()
    private var _binding: FragmentFeedBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.suggestedAccountsRv.adapter = SuggestedAccountsAdapter().apply {
            setOnItemClickListener {
                Log.d(TAG, "onViewCreated: $it")
            }

            submitList((1..100).toList())
        }


    }


    companion object {
        private const val TAG = "FeedFragment"
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}