package com.feup.cpm.ticketbuy.controllers.utils

import QRCodeDialogFragment
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

fun Fragment.handleBackPressed() {
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val fragmentManager = requireActivity().supportFragmentManager
            val fragment = fragmentManager.findFragmentByTag("QRCodeDialog")
            if (fragment is QRCodeDialogFragment) {
                fragment.dismiss()
            } else {
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }
    })
}
