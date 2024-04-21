package com.feup.cpm.ticketbuy.ui.performances

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class PerformancesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val performancesViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(PerformancesViewModel::class.java)


        // Define root layout as a ViewGroup
        val layoutResourceId = resources.getIdentifier("fragment_performances", "layout", activity?.applicationContext?.packageName)
        val root = inflater.inflate(layoutResourceId, container, false) as ViewGroup

        data class Performance(val title: String, val date: String, val price: String, val imageResource: Int)

        // Performances data (to be replaced with actual data from the server)
        val performances = listOf(
            Performance("Performance Title 1", "Performance Date 1", "$100", 1),
            Performance("Performance Title 2", "Performance Date 2", "$120", 2),
            Performance("Performance Title 3", "Performance Date 3", "$80", 3),
            Performance("Performance Title 4", "Performance Date 4", "$90", 4)
        )

        // Find views for each performance
        val performanceViews = mutableListOf<View>()
        for (i in 1..4) {
            val performanceViewModel = ViewModelProvider(
                this,
                ViewModelProvider.NewInstanceFactory()
            ).get(PerformanceViewModel::class.java)
        }

        // Populate data for each performance
        for ((index, performance) in performances.withIndex()) {
            val performanceView = performanceViews[index]

            val imageViewPerformance = performanceView.findViewById<ImageView>(0/* Replace with ImageView ID */)
            val textViewPerformanceTitle = performanceView.findViewById<TextView>(0/* Replace with TextView ID */)
            val textViewPerformanceDate = performanceView.findViewById<TextView>(0/* Replace with TextView ID */)
            val textViewPerformancePrice = performanceView.findViewById<TextView>(0/* Replace with TextView ID */)
            val buttonBuyTickets = performanceView.findViewById<Button>(0/* Replace with Button ID */)

            // Set data for the performance
            // imageViewPerformance.setImageResource(performance.imageResource)
            textViewPerformanceTitle.text = performance.title
            textViewPerformanceDate.text = performance.date
            textViewPerformancePrice.text = performance.price

            // Set click listener for buying tickets
            buttonBuyTickets.setOnClickListener {
                // Handle buy tickets action
                showToast("Buying tickets for ${performance.title}")
            }

            // Add the performance view to the root layout
            root.addView(performanceView)
        }

        return root
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun ViewModelProvider.get(java: Class<PerformanceViewModel>): PerformanceViewModel {
        return this.get(PerformanceViewModel::class.java)
    }

}