package com.best.now.autoclick.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.SurfaceHolder
import android.view.View
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.camera.CameraManager
import com.best.now.autoclick.databinding.ActivityProtractorBinding
import com.best.now.autoclick.databinding.ActivityRulerBinding
import com.best.now.autoclick.view.RulerView
import java.io.IOException

class RulerActivity:BaseVMActivity() {
    private val binding by binding<ActivityRulerBinding>(R.layout.activity_ruler)
    override fun initView() {
        binding.apply {
            flBack.setOnClickListener { onBackPressed() }
            flParent.addView(RulerView(this@RulerActivity))
        }
    }

    override fun initData() {

    }


}