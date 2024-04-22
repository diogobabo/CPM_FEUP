package com.feup.cpm.ticketbuy.ui.performances

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

class PerformancesFragment : Fragment() {

    private val viewModel: TicketsViewModel by activityViewModels()
    data class Performance(val title: String, val date: String, val price: Int)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootLayout = LinearLayout(requireContext())
        rootLayout.orientation = LinearLayout.VERTICAL

        val performances = listOf(
            Performance("Performance Title 1", "Performance Date 1", 100),
            Performance("Performance Title 2", "Performance Date 2", 120),
            Performance("Performance Title 3", "Performance Date 3", 80),
            Performance("Performance Title 4", "Performance Date 4", 90)
        )

        performances.forEach { performance ->
            val performanceLayout = createPerformanceLayout(inflater, container, performance)
            rootLayout.addView(performanceLayout)
        }

        return rootLayout
    }

    private fun createPerformanceLayout(inflater: LayoutInflater, container: ViewGroup?, performance: Performance): View {
        val performanceLayout = LinearLayout(requireContext())
        performanceLayout.orientation = LinearLayout.VERTICAL

        val titleTextView = TextView(requireContext())
        titleTextView.text = performance.title
        performanceLayout.addView(titleTextView)

        val dateTextView = TextView(requireContext())
        dateTextView.text = performance.date
        performanceLayout.addView(dateTextView)

        val priceTextView = TextView(requireContext())
        priceTextView.text = performance.price.toString() + "€"
        performanceLayout.addView(priceTextView)

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

        val buyButton = Button(requireContext())
        buyButton.text = "Buy Tickets"
        buyButton.setOnClickListener {
            val numTickets = ticketsEditText.text.toString().toIntOrNull() ?: 0
            if (numTickets > 0 && numTickets <= 4) {
                val totalCost = numTickets * performance.price.toInt()
                showConfirmationDialog(performance.title, numTickets, totalCost, performance)
            } else {
                showToast("Please enter a valid number of tickets (1-4) for ${performance.title}")
            }
        }
        performanceLayout.addView(buyButton)

        return performanceLayout
    }

    private fun showConfirmationDialog(title: String, numTickets: Int, totalCost: Int, performance: Performance) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Purchase")
            .setMessage("Do you want to buy $numTickets ticket(s) for $title? Total cost: $totalCost€")
            .setPositiveButton("Yes") { dialog, _ ->
                showToast("Buying $numTickets ticket(s) for $title. Total cost: $totalCost€")
                viewModel.addTicket(performance)
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
