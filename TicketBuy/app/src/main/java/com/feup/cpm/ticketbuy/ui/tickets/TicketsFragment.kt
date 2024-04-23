package com.feup.cpm.ticketbuy.ui.tickets

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.feup.cpm.ticketbuy.R
import com.feup.cpm.ticketbuy.controllers.Controller
import com.feup.cpm.ticketbuy.databinding.FragmentTicketsBinding
import com.feup.cpm.ticketbuy.models.Ticket

class TicketsFragment : Fragment() {
    private var _binding: FragmentTicketsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTicketsBinding.inflate(inflater, container, false)
        val rootLayout: View = binding.root

        // Observe the allTickets LiveData
        Controller.allTickets.observe(viewLifecycleOwner, Observer { tickets ->
            // Update the UI with the tickets data
            updateUIWithTickets(tickets)
        })

        return rootLayout
    }

    @SuppressLint("SetTextI18n")
    private fun updateUIWithTickets(tickets: List<Ticket>) {
        val ticketsLayout = binding.ticketsLayout // This should work now
        ticketsLayout.removeAllViews() // Clear existing views

        for (ticket in tickets) {
            val ticketView = LayoutInflater.from(context).inflate(
                R.layout.ticket_item, // Layout for each ticket item
                ticketsLayout,
                false
            )

            val ticketIdTextView = ticketView.findViewById<TextView>(R.id.ticketIdTextView)
            val performanceNameTextView = ticketView.findViewById<TextView>(R.id.performanceNameTextView)
            val userIdTextView = ticketView.findViewById<TextView>(R.id.userIdTextView)
            val placeInRoomTextView = ticketView.findViewById<TextView>(R.id.placeInRoomTextView)

            ticketIdTextView.text = "Ticket ID: ${ticket.ticketId}"
            performanceNameTextView.text = "Performance Name: ${ticket.performance.name}"
            userIdTextView.text = "User ID: ${ticket.userId}"
            placeInRoomTextView.text = "Place in Room: ${ticket.placeInRoom}"

            ticketsLayout.addView(ticketView)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
