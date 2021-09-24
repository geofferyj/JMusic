package com.geofferyj.jmusic.payment.submit_proof

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.load
import com.geofferyj.jmusic.databinding.FragmentSubmitProofBinding
import com.geofferyj.jmusic.ui.activities.MainActivity
import com.geofferyj.jmusic.utils.copyText
import com.geofferyj.jmusic.utils.shortString
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*

class SubmitProofFragment : Fragment() {
    private lateinit var binding: FragmentSubmitProofBinding
    private val args: SubmitProofFragmentArgs by navArgs()

    private var proofUri: Uri? = null

    private val getProof =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            binding.proof.load(uri)
            binding.btnSubmit.text = "Submit"

            proofUri = uri

        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmitProofBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCopy = binding.btnCopy
        val btnSubmit = binding.btnSubmit
        val accountDetails = args.accountDetails
        binding.details = accountDetails

        btnCopy.setOnClickListener {

            val details =
                "${accountDetails?.bankName}\n${accountDetails?.accountName}\n${accountDetails?.accountNumber}"

            copyText(details, requireActivity())
        }

        btnSubmit.setOnClickListener {
            if ((it as MaterialButton).text.toString() != "Submit") {
                getProof.launch("image/*")
            } else {

                submitProof(proofUri)
            }
        }
    }

    private fun submitProof(uri: Uri?) {
        binding.btnSubmit.isEnabled = false
        binding.progress.isVisible = true

        var filename = ""
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

        if (uri != null) {
            requireContext().contentResolver.query(uri, projection, null, null, null)
                .use { metaCursor ->
                    if (metaCursor != null) {
                        if (metaCursor.moveToFirst()) {
                            filename = metaCursor.getString(0)
                        }
                    }
                }
            val extension = filename.split(".").last()
            val userEmail = Firebase.auth.currentUser?.email

            Firebase.storage.reference
                .child("transactionProofs/$userEmail/${Date().shortString}.$extension")
                .putFile(uri)
                .addOnSuccessListener {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                .addOnFailureListener {
                    binding.btnSubmit.isEnabled = true
                    binding.progress.isVisible = false

                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()

                }
        }


    }

    companion object {
        private const val TAG = "SubmitProofFragment"
    }
}