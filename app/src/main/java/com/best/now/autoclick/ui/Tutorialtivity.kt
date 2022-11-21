package com.best.now.autoclick.ui

import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivityTutorialBinding
import com.best.now.autoclick.utils.loadAd

/**
author:zhoujingjin
date:2022/11/20
 */
class TutorialActivity:BaseVMActivity() {
    private val binding by binding<ActivityTutorialBinding>(R.layout.activity_tutorial)
    override fun initView() {
        binding.apply {
            setSupportActionBar(toolBar)
            toolBar.setNavigationOnClickListener { finish() }
        }
    }

    override fun initData() {
        loadAd(binding.advBanner)
    }
}