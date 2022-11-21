package com.best.now.autoclick.ui

import android.util.Log
import android.widget.Toast
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivitySingleInstructionBinding

/**
author:zhoujingjin
date:2022/11/19
 */
class SingleInstructionActivity:BaseVMActivity() {
    private val binding by binding<ActivitySingleInstructionBinding>(R.layout.activity_single_instruction)
    override fun initView() {
        binding.apply {
            setSupportActionBar(toolBar)
            toolBar.setNavigationOnClickListener { finish() }
        }
    }

    override fun initData() {
    }
}