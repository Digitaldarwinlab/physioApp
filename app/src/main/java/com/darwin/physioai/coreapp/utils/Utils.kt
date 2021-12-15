package com.darwin.physioai.coreapp.utils


import android.view.View
import androidx.fragment.app.Fragment
import com.example.physioai.data.network.Resource

import com.google.android.material.snackbar.Snackbar

fun View.snackbar(message: String, action: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    action?.let {
        snackbar.setAction("Retry") {
            it()
        }
    }
//    snackbar.setBackgroundTint(resources.getColor(R.color.colorPrimaryDark))
//    snackbar.setTextColor(Color.WHITE)
//    snackbar.setActionTextColor(resources.getColor(R.color.yellow))
//    snackbar.show()
 
}

fun Fragment.handleApiError(failure: Resource.Failure, retry: (() -> Unit)? = null) {
    when {
        failure.isNetworkError -> requireView().snackbar(
            "Please check your internet connection",
            retry
        )
        failure.errorCode == 401 -> {
//            if (this is LoginFragment) {
//                requireView().snackbar("You've entered incorrect email or password")
//            } else {
//                (this as BaseFragment<*, *, *>).logout()
//            }
        }

        else -> {
            val error = failure.errorBody?.string().toString()
            requireView().snackbar(error)
        }
    }

}

