package com.best.now.autoclick.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivityCommonSettingBinding
import com.best.now.autoclick.ext.dp
import com.best.now.autoclick.ext.getSpValue
import com.best.now.autoclick.ext.putSpValue
import com.best.now.autoclick.view.PointView

class CommonSettingActivity:BaseVMActivity() {
    private val binding by binding<ActivityCommonSettingBinding>(R.layout.activity_common_setting)
    override fun initView() {
        val view = PointView(this@CommonSettingActivity, bigWith = getSpValue("viewSize",60).dp.toInt(), withText = false)

        binding.apply {
            sbView.progress = getSpValue("viewSize",60)
            flView.addView(view)
            sbView.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    putSpValue("viewSize",p1)
                    view.changeSize(p1.dp.toInt())
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            })
           var now =  getSpValue("controllerSize",1f)
            if (now!=1f){
                setViewScale(llControl,1f,now)
            }
            when(now){
                0.5f->sbController.progress = 0
                1f->sbController.progress = 100
                0.75f->sbController.progress = 50
            }
            sbController.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    now = if (sbController.progress <25){
                        0.5f
                    }else if (sbController.progress>=75){
                        1f
                    }else{
                        0.75f
                    }
                    setViewScale(llControl,getSpValue("controllerSize",1f),now)
                    putSpValue("controllerSize",now)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    if (sbController.progress <25){
                        sbController.progress = 0
                        now = 0.5f
                    }else if (sbController.progress>=75){
                        sbController.progress = 100
                        now = 1f
                    }else{
                        sbController.progress = 50
                        now = 0.75f
                    }
                    setViewScale(llControl,getSpValue("controllerSize",1f),now)
                    putSpValue("controllerSize",now)
                }
            })
            setSupportActionBar(toolBar)
            toolBar.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }

    fun setViewScale(view: View,start:Float,end:Float){
        val scaleX = ObjectAnimator.ofFloat(view,"scaleX",start,end)
        val scaleY = ObjectAnimator.ofFloat(view,"scaleY",start,end)
        val set = AnimatorSet()
        set.play(scaleX).with(scaleY)
        set.duration = 100
        set.start()
    }

    override fun initData() {

    }
}