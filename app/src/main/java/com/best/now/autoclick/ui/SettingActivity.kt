package com.best.now.autoclick.ui

import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivitySettingBinding
import com.best.now.autoclick.utils.Constant
import com.best.now.autoclick.utils.loadAd

/**
author:zhoujingjin
date:2022/11/20
 */
class SettingActivity:BaseVMActivity() {
    private val binding by binding<ActivitySettingBinding>(R.layout.activity_setting)
    override fun initView() {
        binding.apply {
            ivPolicy.setOnClickListener {
                WebActivity.startActivity(
                    this@SettingActivity,
                    "Privacy Policy",
                    Constant.URL_PRIVACY_POLICY
                )
            }
            ivService.setOnClickListener {
                WebActivity.startActivity(
                    this@SettingActivity,
                    "Terms of Service",
                    Constant.URL_TERMS_OF_USE
                )
            }
            toolBar.title = ""
            setSupportActionBar(toolBar)
            toolBar.setNavigationOnClickListener { finish() }
        }
    }


    override fun initData() {
        loadAd(binding.advBanner)
    }


}