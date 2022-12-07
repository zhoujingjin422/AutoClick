package com.best.now.autoclick.ui

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
            setSupportActionBar(toolBar)
            toolBar.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }

    override fun initData() {

    }
}