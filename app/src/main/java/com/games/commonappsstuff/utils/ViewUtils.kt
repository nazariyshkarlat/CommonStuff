package com.games.commonappsstuff.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.contains
import androidx.core.view.marginBottom
import androidx.core.view.setMargins
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.games.commonappsstuff.App
import com.games.commonappsstuff.R
import com.games.commonappsstuff.connection.backend.PopupInfoEntity
import com.games.commonappsstuff.presentation.fragment.ErrorDialogAlertFragment
import com.games.commonappsstuff.presentation.fragment.base.BaseDialogAlertFragment
import kotlinx.android.synthetic.main.popup_layout.*
import kotlinx.android.synthetic.main.popup_layout.view.*
import java.util.logging.Handler

object ViewUtils {

    @SuppressLint("ClickableViewAccessibility")
    fun AppCompatActivity.showPopup() {

        PrefsUtils.getPopupInfo()?.let {
            App.sendAmplitudeMessage("show popup")

            PrefsUtils.setPopupWasShown()
            App.popupWasShown = true

            val rootView =
                this.window.decorView.rootView.findViewById(android.R.id.content) as ViewGroup

            if (rootView.findViewById<View?>(R.id.popupContainerLayout) == null) {
                val popup = layoutInflater.inflate(
                    R.layout.popup_layout,
                    popupContainerLayout
                )

                with(popup) {

                    popupLayoutText.text = it.popupText
                    popupLayoutSwitch.text = it.switchText

                    popupLayoutCloseIcon.setOnClickListener {
                        hidePopup()
                    }

                    popupLayoutSwitch.setOnCheckedChangeListener { _, b ->
                        if(b){
                            hidePopup()

                            ErrorDialogAlertFragment().apply {
                                arguments = bundleOf("ERROR_TEXT" to it.errorText)
                            }.show(supportFragmentManager, null)

                        }
                    }

                    popupLayout.setOnTouchListener { _, _ -> true }
                    setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN)
                            hidePopup()
                        true
                    }
                }

                val oldView: View? = rootView.findViewById(R.id.popupContainerLayout)
                oldView?.let {
                    rootView.removeView(it)
                }
                if (!rootView.contains(popup)) {
                    rootView.addView(popup)
                    popup.popupLayout.measure(0, 0)
                    popup.popupLayout.translationY =
                        (popup.popupLayout.measuredHeight + popup.popupLayout.marginBottom).toFloat()
                    popup.alpha = 0F
                    popup.animate().setStartDelay(500).alpha(1F).setListener(null).start()
                    popup.popupLayout.animate().setStartDelay(500).translationY(0F)
                        .setInterpolator(OvershootInterpolator()).setListener(null).setDuration(600)
                        .start()
                }
            }
        }
    }

    fun Activity.hidePopup(){

        val rootView = this.window.decorView.rootView.findViewById(android.R.id.content) as ViewGroup

        val popup= rootView.findViewById<View?>(R.id.popupContainerLayout)

        popup?.let {
            it.animate().alpha(0F).setStartDelay(0).setDuration(600).setInterpolator(FastOutSlowInInterpolator()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    rootView.removeView(it)
                }
            }).start()
        }
    }

}