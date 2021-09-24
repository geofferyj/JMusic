package com.geofferyj.jmusic.auth.register

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.geofferyj.jmusic.auth.AuthActivity
import com.geofferyj.jmusic.auth.AuthViewModel
import com.geofferyj.jmusic.databinding.RegisterFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment() {

    private lateinit var binding: RegisterFragmentBinding
    private val args: RegisterFragmentArgs by navArgs()
    private val viewModel: AuthViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RegisterFragmentBinding.inflate(inflater, container, false)
        binding.email.editText?.setText(args.email)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val login = binding.login
        val loading = binding.loading


        login.setOnClickListener {
            val name = binding.name.editText?.text.toString()
            val email = binding.email.editText?.text.toString()
            val password = binding.password.editText?.text.toString()
            val confirmPassword = binding.confirmPassword.editText?.text.toString()

            if (canProceed(name, email, password, confirmPassword)) {
                loading.visibility = View.VISIBLE
                lifecycleScope.launchWhenCreated {
                    val user = viewModel.signUp(name, email, password)

                    if (user.userId.isBlank()) {
                        requireActivity().setResult(RESULT_CANCELED)
                        requireActivity().finish()
                    } else {
                        val returnIntent = Intent()
                        returnIntent.putExtra(AuthActivity.RESULT_STATUS, true)
                        requireActivity().setResult(RESULT_OK, returnIntent)
                        requireActivity().finish()
                    }
                }
            }

        }
    }

    private fun inputIsValid(vararg strings: String) = strings.all {
        it.isNotBlank()
    }

    private fun canProceed(name: String, email: String, password: String, confirmPassword: String): Boolean {
        val emailPattern = """[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+"""
        val inputsValid = inputIsValid(name, email, password)


        return if (!email.matches(emailPattern.toRegex())) {
            binding.email.error = "Please enter a valid email"
            false
        } else if (!inputsValid){
            binding.email.error = if (email.isBlank()) "Email is required" else null
            binding.password.error = if (password.isBlank()) "Password is required" else null
            false
        } else if (password != confirmPassword){
            binding.confirmPassword.error = "Passwords don't match"
            false
        }else{
            binding.email.error = null
            binding.password.error = null
            true
        }

    }
}