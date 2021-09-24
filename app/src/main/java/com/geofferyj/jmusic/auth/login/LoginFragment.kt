package com.geofferyj.jmusic.auth.login

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.geofferyj.jmusic.R
import com.geofferyj.jmusic.auth.AuthActivity
import com.geofferyj.jmusic.auth.AuthViewModel
import com.geofferyj.jmusic.auth.register.RegisterFragmentArgs
import com.geofferyj.jmusic.databinding.LoginFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment() {


    private val viewModel: AuthViewModel by viewModel()
    private lateinit var binding: LoginFragmentBinding
    private val args: LoginFragmentArgs by navArgs()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginFragmentBinding.inflate(inflater, container, false)
        binding.email.editText?.setText(args.email)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val login = binding.login
        val loading = binding.loading


        login.setOnClickListener {
            val email = binding.email.editText?.text.toString()
            val password = binding.password.editText?.text.toString()

            if (canProceed(email, password)) {
                loading.visibility = View.VISIBLE

                lifecycleScope.launchWhenCreated {
                    val userId = viewModel.signIn(email, password)

                    if (userId.isNullOrBlank()) {
                        //TODO: Show snackBar
                    } else {

                        val returnIntent = Intent()
                        returnIntent.putExtra(AuthActivity.RESULT_STATUS, true)
                        requireActivity().setResult(RESULT_OK, returnIntent)
                        requireActivity().finish()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "All fields must contain valid values",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun inputIsValid(vararg strings: String) = strings.all {
        it.isNotBlank()
    }

    private fun canProceed(email: String, password: String): Boolean {
        val emailPattern = """[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+"""
        val inputsValid = inputIsValid(email, password)


        return if (!email.matches(emailPattern.toRegex())) {
            binding.email.error = "Please enter a valid email"
            false
        } else if (!inputsValid){
            binding.email.error = "Email is required"
            binding.password.error = "Password is required"
            false
        }else{
            binding.email.error = null
            binding.password.error = null
            true
        }

    }
}