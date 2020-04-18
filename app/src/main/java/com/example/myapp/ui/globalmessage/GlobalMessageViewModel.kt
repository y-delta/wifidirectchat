package com.example.myapp.ui.globalmessage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Global Message page in kotlin"
    }
    val text: LiveData<String> = _text
}