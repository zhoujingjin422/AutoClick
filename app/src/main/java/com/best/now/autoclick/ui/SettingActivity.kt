package com.best.now.autoclick.ui

import android.content.Intent
import android.view.View
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivitySettingBinding
import com.best.now.autoclick.utils.Constans
import com.best.now.autoclick.utils.isPurchased
import com.best.now.autoclick.utils.loadAd
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.TimeUtils

/**
author:zhoujingjin
date:2022/11/20
 */
class SettingActivity:BaseVMActivity() {
    private val binding by binding<ActivitySettingBinding>(R.layout.activity_setting)
    override fun initView() {
        binding.apply {
            setUiState()
            flPolicy.setOnClickListener {
                WebActivity.startActivity(
                    this@SettingActivity,
                    "Privacy Policy",
                    Constans.URL_PRIVACY_POLICY
                )
            }
            flService.setOnClickListener {
                WebActivity.startActivity(
                    this@SettingActivity,
                    "Terms of Service",
                    Constans.URL_TERMS_OF_USE
                )
            }
            setSupportActionBar(toolBar)
            toolBar.setNavigationOnClickListener { finish() }
        }
    }

    private fun setUiState() {
        if (!MainActivity.purchased){
            binding.ivVipCenter.visibility = View.VISIBLE
            binding.btnGetVip.visibility = View.VISIBLE
            binding.llText.visibility = View.GONE
            binding.ivVipRight.visibility = View.GONE
            binding.btnGetVip.setOnClickListener {
                startActivity(Intent(this@SettingActivity,SubscribeActivity::class.java))
            }
        }else{
            binding.llText.visibility = View.VISIBLE
            binding.ivVipRight.visibility = View.VISIBLE
            binding.ivVipCenter.visibility = View.GONE
            binding.btnGetVip.visibility = View.GONE
            var time = 0L
            if (MainActivity.productId.contains(Constans.VIP_MONTH)) {
                time = 30 * 24 * 3600 * 1000L
            } else if (MainActivity.productId.contains(Constans.VIP_HALF_YEAR)) {
                time = 6*30 * 24 * 3600 * 1000L
            } else if (MainActivity.productId.contains(Constans.VIP_YEAR)) {
                time = 365 * 24 * 3600 * 1000L
            }
            binding.tvDate.text = "Membership valid until：${TimeUtils.millis2String(
                MainActivity.purchaseTime + time,
                "yyyy.MM.dd"
            )}"

        }

    }

    override fun initData() {
        loadAd(binding.advBanner)
    }

    @BusUtils.Bus(tag = MainActivity.BUS_TAG_UPDATE_PURCHASE_STATE)
    fun updateState() {
        isPurchased(this)
        setUiState()
    }
}