package com.goldenpiedevs.schedule.app.ui.preference

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat

class TimePickerDialogFragment : PreferenceDialogFragmentCompat() {

    companion object {
        fun getInstance(key : String?) : TimePickerDialogFragment {
            val fragment = TimePickerDialogFragment()
            val bundle = Bundle(1)
            bundle.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // TODO : A Custom view I guess
        val dialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }, 6, 0, true)
        return dialog
    }

}