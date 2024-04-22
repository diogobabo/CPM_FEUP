package com.feup.cpm.ticketbuy.ui.tickets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.feup.cpm.ticketbuy.ui.performances.PerformancesFragment
class TicketsViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text
    var boughtTicketsList = mutableListOf<PerformancesFragment.Performance>()
    public fun addTicket(performance: PerformancesFragment.Performance){
        boughtTicketsList.add(performance)
    }
}

