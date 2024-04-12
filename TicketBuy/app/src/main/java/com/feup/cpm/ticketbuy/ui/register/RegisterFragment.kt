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
import java.util.Calendar
import android.widget.Spinner
import android.widget.ArrayAdapter


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
        val etCardType: Spinner = binding.etCardType
        val etCardNumber: EditText = binding.etCardNumber
        val etCardValidityMonth: EditText = binding.etCardValidityMonth
        val etCardValidityYear: EditText = binding.etCardValidityYear
        val btnRegister: Button = binding.btnRegister

        // Initialize Spinner for Card Type
        val cardTypeOptions = arrayOf("Visa", "Mastercard")
        val cardTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cardTypeOptions)
        cardTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        etCardType.adapter = cardTypeAdapter

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val nif = etNIF.text.toString()
            val cardType = etCardType.selectedItem.toString()
            val cardNumber = etCardNumber.text.toString()
            val cardValidityMonth = etCardValidityMonth.text.toString()
            val cardValidityYear = etCardValidityYear.text.toString()

            if (validateInputs(name, nif, cardType, cardNumber, cardValidityMonth, cardValidityYear)) {
                // Handle registration here
                showToast("Registration Successful")
            } else {
                //showToast("Please fill in all fields correctly")
            }
        }

        return root
    }

    private fun validateInputs(
        name: String,
        nif: String,
        cardType: String,
        cardNumber: String,
        cardValidityMonth: String,
        cardValidityYear: String
    ): Boolean {
        if (name.isBlank()) {
            showToast("Name cannot be empty")
            return false
        }
        if (!nif.isValidNIF()) {
            showToast("NIF must be a number with exactly 9 digits")
            return false
        }
        if (!cardType.isValidCardType()) {
            showToast("Invalid card type. Please select Visa or Mastercard")
            return false
        }
        if (!cardNumber.isValidCardNumber()) {
            showToast("Card number must have 16-19 digits")
            return false
        }
        if (!cardValidityMonth.isValidCardValidityMonth()) {
            showToast("Invalid card validity month. Please enter a valid month (1-12)")
            return false
        }
        if (!cardValidityYear.isValidCardValidityYear()) {
            showToast("Invalid card validity year. Please enter a valid year")
            return false
        }
        return true
    }

    private fun String.isValidNIF(): Boolean {
        return this.matches("\\d{9}".toRegex())
    }

    private fun String.isValidCardType(): Boolean {
        return this.equals("Visa", ignoreCase = true) || this.equals("Mastercard", ignoreCase = true)
    }

    private fun String.isValidCardNumber(): Boolean {
        return this.matches("\\d{16,19}".toRegex())
    }

    private fun String.isValidCardValidityMonth(): Boolean {
        return this.toIntOrNull() in 1..12
    }

    private fun String.isValidCardValidityYear(): Boolean {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return (this.toIntOrNull() ?: 0) >= currentYear
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}
