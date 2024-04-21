package com.feup.cpm.ticketbuy.ui.performances

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class PerformanceViewModel {

    data class Performance(val title: String, val date: String, val price: String, val imageResource: Int)

    fun createPerformanceView(
        performance: Performance,
        parent: ViewGroup,
        context: Context
    ): View {
        val performanceView = LinearLayout(context)
        performanceView.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        performanceView.layoutParams = params
        performanceView.setPadding(16, 16, 16, 16)

        val textViewPerformanceTitle = TextView(context)
        textViewPerformanceTitle.text = performance.title
        textViewPerformanceTitle.textSize = 18f
        performanceView.addView(textViewPerformanceTitle)

        val textViewPerformanceDate = TextView(context)
        textViewPerformanceDate.text = performance.date
        performanceView.addView(textViewPerformanceDate)

        val textViewPerformancePrice = TextView(context)
        textViewPerformancePrice.text = performance.price
        performanceView.addView(textViewPerformancePrice)

        val buttonBuyTickets = Button(context)
        buttonBuyTickets.text = "Buy Tickets"
        buttonBuyTickets.setOnClickListener {
            // Handle buy tickets action
            showToast(context, "Buying tickets for ${performance.title}")
        }
        performanceView.addView(buttonBuyTickets)

        parent.addView(performanceView)

        return performanceView
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}