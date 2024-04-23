package com.feup.cpm.ticketbuy.ui.cafeteria

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CafeteriaViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is cafeteria Fragment"
    }
    val text: LiveData<String> = _text
}