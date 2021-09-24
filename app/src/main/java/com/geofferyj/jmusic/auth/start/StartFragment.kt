package com.geofferyj.jmusic.auth.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.geofferyj.jmusic.auth.AuthViewModel
import com.geofferyj.jmusic.databinding.FragmentStartBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartFragment : Fragment() {

    private lateinit var binding: FragmentStartBinding
    private val viewModel: AuthViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueBtn.setOnClickListener {
            val email = binding.email.editText?.text.toString()

            if (!canProceed(email)) return@setOnClickListener
            binding.continueBtn.isEnabled = false
            binding.loading.visibility = View.VISIBLE

            lifecycleScope.launchWhenCreated {
                withContext(Dispatchers.IO) {
                    val userInfoExists = viewModel.userExists(email)

                    val action = if (userInfoExists) {

                        StartFragmentDirections.actionStartFragmentToLoginFragment(email)

                    } else {
                        StartFragmentDirections.actionStartFragmentToRegisterFragment(email)
                    }

                    withContext(Dispatchers.Main) {
                        findNavController().navigate(action)
                    }

                }
            }
        }
    }

    private fun canProceed(email: String): Boolean {
        val emailPattern = """[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+"""

        return if (!email.matches(emailPattern.toRegex())) {
            binding.email.error = "Please enter a valid email"
            false
        }else{
            binding.email.error = null
            true
        }

    }

}