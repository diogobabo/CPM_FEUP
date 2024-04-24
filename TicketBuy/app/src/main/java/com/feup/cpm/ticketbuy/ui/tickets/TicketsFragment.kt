package com.feup.cpm.ticketbuy.ui.tickets

import QRCodeDialogFragment
import android.annotation.SuppressLint
import android.graphics.Bitmap
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
import com.feup.cpm.ticketbuy.controllers.cypher.KeyManager
import com.feup.cpm.ticketbuy.controllers.utils.QRCodeGenerator
import com.feup.cpm.ticketbuy.controllers.utils.TagInfo
import com.feup.cpm.ticketbuy.databinding.FragmentTicketsBinding
import com.feup.cpm.ticketbuy.models.Ticket
import org.json.JSONObject

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

        Controller.getTickets()
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

            if (ticket.isUsed) {
                ticketIdTextView.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            } else {
                ticketIdTextView.setTextColor(resources.getColor(android.R.color.black))
            }
            // Add click listener to generate and show QR code
            ticketView.setOnClickListener {
                generateAndShowQRCode(ticket)
            }

            ticketsLayout.addView(ticketView)
        }
    }
    private fun generateAndShowQRCode(ticket: Ticket) {
        // Create the payload JSON object
        val payloadJson = JSONObject()
        payloadJson.put("ticketId", ticket.ticketId)
        payloadJson.put("performance_id", ticket.performance.performanceId)

        payloadJson.put("signature", KeyManager.singData(payloadJson.toString().toByteArray()))

        val tagInfo = TagInfo(
            tagId = "TICKET ${ticket.ticketId}", // You can generate a unique tagId if needed
            status = true,
            tagType = "Ticket",
            userId = ticket.userId,
            payLoad = payloadJson
        )

        // Convert the TagInfo object to JSON string
        val tagInfoJsonString = tagInfo.toJsonString()

        // Generate QR code
        val width = resources.getDimensionPixelSize(R.dimen.qr_code_width)
        val height = resources.getDimensionPixelSize(R.dimen.qr_code_height)
        val qrCodeBitmap: Bitmap? = QRCodeGenerator.generateQRCode(tagInfoJsonString, width, height)

        qrCodeBitmap?.let {
            val dialogFragment = QRCodeDialogFragment(it)
            dialogFragment.show(requireActivity().supportFragmentManager, "QRCodeDialog")
        }
    }

    private fun TagInfo.toJsonString(): String {
        return JSONObject(
            mapOf(
                "tagId" to tagId,
                "status" to status,
                "tagType" to tagType,
                "userId" to userId,
                "payLoad" to payLoad
            )
        ).toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}