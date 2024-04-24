package com.feup.cpm.ticketbuy.ui.performances

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.feup.cpm.ticketbuy.R
import com.feup.cpm.ticketbuy.controllers.Controller
import com.feup.cpm.ticketbuy.models.Performance

class PerformancesFragment : Fragment() {
    companion object {
        private const val BIOMETRIC_PERMISSION_REQUEST_CODE = 100
    }

    private var controller = Controller
    private lateinit var rootLayout: LinearLayout

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootLayout = LinearLayout(requireContext())
        rootLayout.orientation = LinearLayout.VERTICAL

        controller.performances.value?.forEach { performance ->
            val performanceLayout = createPerformanceLayout(inflater, container, performance)
            rootLayout.addView(performanceLayout)
        }

        return rootLayout
    }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe changes to performances LiveData
        controller.performances.observe(viewLifecycleOwner) { updatedPerformances ->
            // Clear existing views
            rootLayout.removeAllViews()

            // Add new performance views
            updatedPerformances.forEach { performance ->
                val performanceLayout = createPerformanceLayout(
                    LayoutInflater.from(context),
                    null,
                    performance
                )
                rootLayout.addView(performanceLayout)
            }
        }

        // Trigger fetching performances
        controller.getNextPerformances()
    }
    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("SetTextI18n")
    private fun createPerformanceLayout(inflater: LayoutInflater, container: ViewGroup?, performance: Performance): View {
        val performanceLayout = inflater.inflate(R.layout.performance_item, null, false) as LinearLayout
        performanceLayout.orientation = LinearLayout.VERTICAL

        val titleTextView = performanceLayout.findViewById<TextView>(R.id.titleTextView)
        titleTextView.text = performance.name

        val dateTextView = performanceLayout.findViewById<TextView>(R.id.dateTextView)
        dateTextView.text = performance.date

        val priceTextView = performanceLayout.findViewById<TextView>(R.id.priceTextView)
        priceTextView.text = performance.price.toString() + "€"

        val buyButton = performanceLayout.findViewById<Button>(R.id.buyButton)
        buyButton.setOnClickListener {
            val numTickets = performanceLayout.findViewById<EditText>(R.id.ticketsEditText).text.toString().toIntOrNull() ?: 0
            if (numTickets in 1..4) {
                val totalCost = numTickets * performance.price.toInt()
                showConfirmationDialog(performance.name, numTickets, totalCost, performance)
            } else {
                showToast("Please enter a valid number of tickets (1-4) for ${performance.name}")
            }
        }

        return performanceLayout
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showConfirmationDialog(title: String, numTickets: Int, totalCost: Int, performance: Performance) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Purchase")
            .setMessage("Do you want to buy $numTickets ticket(s) for $title? Total cost: $totalCost€")
            .setPositiveButton("Yes") { dialog, _ ->
                showToast("Buying $numTickets ticket(s) for $title. Total cost: $totalCost€")
                controller.purchaseTickets(performance,numTickets)
                checkBiometricPermission()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkBiometricPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.USE_BIOMETRIC
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.USE_BIOMETRIC),
                BIOMETRIC_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted or not needed
            showBiometricPrompt()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            BIOMETRIC_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    showBiometricPrompt()
                } else {
                    // Permission denied
                    Toast.makeText(
                        requireContext(),
                        "Biometric permission denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Please authenticate to proceed")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(requireContext()),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showToast("Authentication error: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showToast("Authentication succeeded!")
                    // Proceed with the ticket purchase or other actions
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showToast("Authentication failed")
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
