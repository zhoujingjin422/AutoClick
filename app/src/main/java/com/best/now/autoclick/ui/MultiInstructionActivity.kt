package com.best.now.autoclick.ui

import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivityMultiInstructionBinding
import com.best.now.autoclick.databinding.ActivitySingleInstructionBinding

/**
author:zhoujingjin
date:2022/11/19
 */
class MultiInstructionActivity:BaseVMActivity() {
    private val binding by binding<ActivityMultiInstructionBinding>(R.layout.activity_multi_instruction)
    override fun initView() {
        binding.apply {
            setSupportActionBar(toolBar)
            toolBar.setNavigationOnClickListener { finish() }
        }
    }

    override fun initData() {
    }
}