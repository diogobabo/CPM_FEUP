package com.feup.cpm.ticketbuy.ui.register

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.feup.cpm.ticketbuy.controllers.cypher.KeyManager

class RegisterActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getResourceId("activity_register", "layout"))

        val etName = findViewById<EditText>(getResourceId("etName", "id"))
        val etNIF = findViewById<EditText>(getResourceId("etNIF", "id"))
        val etCardType = findViewById<EditText>(getResourceId("etCardType", "id"))
        val etCardNumber = findViewById<EditText>(getResourceId("etCardNumber", "id"))
        val etCardValidity = findViewById<EditText>(getResourceId("etCardValidity", "id"))
        val btnRegister = findViewById<Button>(getResourceId("btnRegister", "id"))

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val nif = etNIF.text.toString()
            val cardType = etCardType.text.toString()
            val cardNumber = etCardNumber.text.toString()
            val cardValidity = etCardValidity.text.toString()

            if (validateInputs(name, nif, cardType, cardNumber, cardValidity)) {
                // Generate and store keys if not already generated

                // Encrypt the sensitive data before storing it
                val encryptedName = encryptData(name.toByteArray())
                val encryptedNif = encryptData(nif.toByteArray())
                val encryptedCardType = encryptData(cardType.toByteArray())
                val encryptedCardNumber = encryptData(cardNumber.toByteArray())
                val encryptedCardValidity = encryptData(cardValidity.toByteArray())

                // For simplicity, we'll just display a toast message
                showToast("Registration Successful")
            }
        }
    }

    private fun validateInputs(
        name: String,
        nif: String,
        cardType: String,
        cardNumber: String,
        cardValidity: String
    ): Boolean {
        // Add your validation logic here
        // For simplicity, we'll just check if all fields are non-empty
        return name.isNotEmpty() && nif.isNotEmpty() && cardType.isNotEmpty()
                && cardNumber.isNotEmpty() && cardValidity.isNotEmpty()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun encryptData(data: ByteArray): ByteArray? {
        // Use the KeyManager to encrypt data
        return KeyManager.singData(data)
    }

    private fun getResourceId(name: String, type: String): Int {
        return resources.getIdentifier(name, type, packageName)
    }
}
