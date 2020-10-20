package com.games.commonappsstuff.presentation.fragment

import android.os.Bundle
import android.view.View
import com.games.commonappsstuff.R
import com.games.commonappsstuff.presentation.fragment.base.BaseDialogAlertFragment
import kotlinx.android.synthetic.main.error_dialog_alert_layout.*

class ErrorDialogAlertFragment : BaseDialogAlertFragment(R.layout.error_dialog_alert_layout){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialogAlertErrorText.text = arguments!!.getString("ERROR_TEXT")

        dialogAlertErrorCloseIcon.setOnClickListener {
            dismiss()
        }
    }

}