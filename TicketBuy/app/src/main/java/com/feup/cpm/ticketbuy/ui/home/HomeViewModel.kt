package com.feup.cpm.ticketbuy.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Ticket / Cafeteria Ordering System"
    }
    val text: LiveData<String> = _text
}