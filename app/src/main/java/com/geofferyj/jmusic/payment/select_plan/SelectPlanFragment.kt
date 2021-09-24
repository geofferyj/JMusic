package com.geofferyj.jmusic.payment.select_plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.geofferyj.jmusic.R
import com.geofferyj.jmusic.databinding.FragmentSelectPlanBinding

class SelectPlanFragment : Fragment() {

    private var _binding: FragmentSelectPlanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSelectPlanBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.free.setOnClickListener {

        }

        binding.premium.setOnClickListener {
            findNavController().navigate(R.id.paymentDialog)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}