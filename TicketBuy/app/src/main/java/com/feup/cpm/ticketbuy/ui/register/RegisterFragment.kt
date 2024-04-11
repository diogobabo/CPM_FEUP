package com.feup.cpm.ticketbuy.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.support.v4.app.Fragment
import android.arch.lifecycle.ViewModelProvider
import com.feup.cpm.ticketbuy.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val registerViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(RegisterViewModel::class.java)

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val etName: EditText = binding.etName
        val etNIF: EditText = binding.etNIF
        val etCardType: EditText = binding.etCardType
        val etCardNumber: EditText = binding.etCardNumber
        val etCardValidity: EditText = binding.etCardValidity
        val btnRegister: Button = binding.btnRegister

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val nif = etNIF.text.toString()
            val cardType = etCardType.text.toString()
            val cardNumber = etCardNumber.text.toString()
            val cardValidity = etCardValidity.text.toString()

            if (validateInputs(name, nif, cardType, cardNumber, cardValidity)) {
                // Handle registration here
                showToast("Registration Successful")
            } else {
                showToast("Please fill in all fields")
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateInputs(
        name: String,
        nif: String,
        cardType: String,
        cardNumber: String,
        cardValidity: String
    ): Boolean {
        return name.isNotBlank() && nif.isNotBlank() && cardType.isNotBlank()
                && cardNumber.isNotBlank() && cardValidity.isNotBlank()
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}
