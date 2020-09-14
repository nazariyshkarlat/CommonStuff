package com.games.commonappsstuff.presentation.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import com.games.commonappsstuff.Const.TIMEOUT
import com.games.commonappsstuff.R
import com.games.commonappsstuff.presentation.fragment.base.BaseFragment
import kotlinx.android.synthetic.main.splash_screen_layout.*


class SplashScreenFragment : BaseFragment(R.layout.splash_screen_layout){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProgressMax(progressBar, 100)
        startAnimation()
    }

    private fun setProgressMax(pb: ProgressBar, max: Int) {
        pb.max = max * 100
    }

    private fun startAnimation() {
        val animation =
            ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, 100 * 100)
        animation.duration = TIMEOUT
        animation.interpolator = LinearInterpolator()
        animation.start()

        val connectingAnimation =
            AnimationUtils.loadAnimation(context, R.anim.oval_anim)
        splashScreenLayoutOval.startAnimation(connectingAnimation)
    }

}