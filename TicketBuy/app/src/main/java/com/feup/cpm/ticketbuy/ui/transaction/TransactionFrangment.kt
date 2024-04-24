package com.feup.cpm.ticketbuy.ui.transaction

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.feup.cpm.ticketbuy.R
import com.feup.cpm.ticketbuy.controllers.Controller
import com.feup.cpm.ticketbuy.models.Transaction

class TransactionFrangment : Fragment() {

    private var controller = Controller
    private lateinit var rootLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootLayout = LinearLayout(requireContext())
        rootLayout.orientation = LinearLayout.VERTICAL

        controller.transactions.value?.forEach { transaction ->
            val transactionLayout = createTransactionLayout(inflater, container, transaction)
            rootLayout.addView(transactionLayout)
        }

        return rootLayout
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe changes to performances LiveData
        controller.transactions.observe(viewLifecycleOwner) { updatedTransactions ->
            // Clear existing views
            rootLayout.removeAllViews()

            // Add new performance views
            updatedTransactions.forEach { transaction ->
                val transactionLayout = createTransactionLayout(
                    LayoutInflater.from(context),
                    null,
                    transaction
                )
                rootLayout.addView(transactionLayout)
            }
        }

        // Trigger fetching performances
        controller.consultTransactions()
    }
    @SuppressLint("SetTextI18n")
    private fun createTransactionLayout(inflater: LayoutInflater, container: ViewGroup?, transaction: Transaction): View {
        val itemLayout = inflater.inflate(R.layout.fragment_transaction, null, false) as LinearLayout
        itemLayout.orientation = LinearLayout.VERTICAL

        val typeTextView = itemLayout.findViewById<TextView>(R.id.typeTextView)
        typeTextView.text = transaction.transactionType

        val dateTextView = itemLayout.findViewById<TextView>(R.id.dateTextView)
        dateTextView.text = transaction.transactionDate

        val priceTextView = itemLayout.findViewById<TextView>(R.id.priceTextView)
        priceTextView.text = transaction.transactionValue.toString() + "€"

        return itemLayout
    }

}
