package com.feup.cpm.ticketbuy.ui.performances

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
import androidx.fragment.app.activityViewModels
import com.feup.cpm.ticketbuy.ui.tickets.TicketsViewModel
import androidx.lifecycle.ViewModelProvider
import com.feup.cpm.ticketbuy.R
import com.feup.cpm.ticketbuy.controllers.Controller
import com.feup.cpm.ticketbuy.models.Performance

class PerformancesFragment : Fragment() {

    private val viewModel: TicketsViewModel by activityViewModels()
    private var controller = Controller
    private lateinit var rootLayout: LinearLayout

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
/*
        val ticketsLayout = LinearLayout(requireContext())
        ticketsLayout.orientation = LinearLayout.HORIZONTAL
        val ticketsTextView = TextView(requireContext())
        ticketsTextView.text = "Tickets: "
        val ticketsEditText = EditText(requireContext())
        ticketsEditText.hint = "Enter number"
        ticketsEditText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        ticketsLayout.addView(ticketsTextView)
        ticketsLayout.addView(ticketsEditText)
        performanceLayout.addView(ticketsLayout)
*/
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

    private fun showConfirmationDialog(title: String, numTickets: Int, totalCost: Int, performance: Performance) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Purchase")
            .setMessage("Do you want to buy $numTickets ticket(s) for $title? Total cost: $totalCost€")
            .setPositiveButton("Yes") { dialog, _ ->
                showToast("Buying $numTickets ticket(s) for $title. Total cost: $totalCost€")
                viewModel.addTicket(performance)
                controller.purchaseTickets(performance.performanceId,performance.date,numTickets)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
