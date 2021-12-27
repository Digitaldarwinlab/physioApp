package com.darwin.physioai.agora

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

import android.os.Looper

import android.os.Bundle
import android.os.Handler
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment


class BaseFragment : Fragment() {
    protected var handler: Handler? = null
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
    }

    protected fun showAlert(message: String?) {
        val context: Context = getContext() ?: return
        AlertDialog.Builder(context).setTitle("Tips").setMessage(message)
            .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            .show()
    }

    protected fun showLongToast(msg: String?) {
        handler?.post(Runnable {
            if (context == null) {
                return@Runnable
            }
            Toast.makeText(context?.getApplicationContext(), msg, Toast.LENGTH_LONG).show()
        })
    }
}