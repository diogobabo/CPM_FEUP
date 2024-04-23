package com.feup.cpm.ticketbuy.ui.cafeteria

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
import com.feup.cpm.ticketbuy.models.Item
import com.feup.cpm.ticketbuy.models.Performance

class CafeteriaFragment : Fragment() {

    //private val viewModel: TicketsViewModel by activityViewModels()
    private var controller = Controller
    private lateinit var rootLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootLayout = LinearLayout(requireContext())
        rootLayout.orientation = LinearLayout.VERTICAL

        controller.items.value?.forEach { item ->
            val itemLayout = createItemLayout(inflater, container, item)
            rootLayout.addView(itemLayout)
        }

        return rootLayout
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe changes to performances LiveData
        controller.items.observe(viewLifecycleOwner) { updatedItems ->
            // Clear existing views
            rootLayout.removeAllViews()

            // Add new performance views
            updatedItems.forEach { item ->
                val itemLayout = createItemLayout(
                    LayoutInflater.from(context),
                    null,
                    item
                )
                rootLayout.addView(itemLayout)
            }
        }

        // Trigger fetching performances
        controller.getCafeteriaItems()
    }
    @SuppressLint("SetTextI18n")
    private fun createItemLayout(inflater: LayoutInflater, container: ViewGroup?, item: Item): View {
        val itemLayout = inflater.inflate(R.layout.fragment_cafeteria, null, false) as LinearLayout
        itemLayout.orientation = LinearLayout.VERTICAL

        val nameTextView = itemLayout.findViewById<TextView>(R.id.nameTextView)
        nameTextView.text = item.name

        val quantityTextView = itemLayout.findViewById<TextView>(R.id.quantityAvailableTextView)
        quantityTextView.text = item.quantity.toString()

        val priceTextView = itemLayout.findViewById<TextView>(R.id.priceTextView)
        priceTextView.text = item.price.toString() + "€"

        val buyButton = itemLayout.findViewById<Button>(R.id.buyButton)
        buyButton.setOnClickListener {
            val numItems = itemLayout.findViewById<EditText>(R.id.itemsEditText).text.toString().toIntOrNull() ?: 0
            if (numItems < item.quantity) {
                val totalCost = numItems * item.price.toInt()
                showConfirmationDialog(item.name, numItems, totalCost, item)
            } else {
                showToast("Please enter a valid number of ${item.name}")
            }
        }

        return itemLayout
    }

    private fun showConfirmationDialog(name: String, numItems: Int, totalCost: Int, item: Item) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Purchase")
            .setMessage("Do you want to buy $numItems $name? Total cost: $totalCost€")
            .setPositiveButton("Yes") { dialog, _ ->
                showToast("Buying $numItems $name. Total cost: $totalCost€")
                //Create add purchase
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
