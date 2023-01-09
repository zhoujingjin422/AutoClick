package com.best.now.autoclick.ui

import android.annotation.SuppressLint
import android.content.Intent
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivityMainBinding
import com.best.now.autoclick.utils.Constant
import com.best.now.autoclick.utils.loadAd

class MainActivity : BaseVMActivity() {

    private val binding by binding<ActivityMainBinding>(R.layout.activity_main)


    @SuppressLint("SuspiciousIndentation")
    override fun initView() {
        binding.apply {

            ivSetting.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }
            ivPlay.setOnClickListener {
                WebPlayActivity.startActivity(
                    this@MainActivity,
                    "",
                    Constant.URL_TRADITIONAL
                )
            }
            ivVideo.setOnClickListener {
                WebPlayActivity.startActivity(
                    this@MainActivity,
                    "",
                    Constant.URL_PIANO
                )
            }
            }
        }
    override fun initData() {
        loadAd(binding.advBanner)
    }
}