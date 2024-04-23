package com.feup.cpm.ticketbuy.ui.tickets

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import com.feup.cpm.ticketbuy.databinding.FragmentHomeBinding
import com.feup.cpm.ticketbuy.databinding.FragmentRegisterBinding
import org.w3c.dom.Text
import com.feup.cpm.ticketbuy.databinding.FragmentTicketsBinding

class TicketsFragment : Fragment() {
    private var _binding: FragmentTicketsBinding? = null
    private val binding get() = _binding!!

    private val ticketsViewModel: TicketsViewModel by activityViewModels()
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTicketsBinding.inflate(inflater, container, false)
        val rootLayout: View = binding.root

        val textView: TextView = binding.textTickets
        ticketsViewModel.boughtTicketsList.forEachIndexed { index, performance ->
            ticketsViewModel.text.observe(viewLifecycleOwner) {
                textView.text = ("Ticket $index: ${performance.name}, ${performance.date}, ${performance.price}€")
            }
        }

        return rootLayout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
