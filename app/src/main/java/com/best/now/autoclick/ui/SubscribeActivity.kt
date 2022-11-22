package com.best.now.autoclick.ui

import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.android.billingclient.api.*
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivitySubscribeBinding
import com.best.now.autoclick.ui.MainActivity.Companion.BUS_TAG_BUY_STATE_PURCHASED
import com.best.now.autoclick.utils.Constant
import com.best.now.autoclick.utils.showInterstitialAd
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils

/**
author:zhoujingjin
date:2022/11/19
 */
class SubscribeActivity:BaseVMActivity() {
    private val binding by binding<ActivitySubscribeBinding>(R.layout.activity_subscribe)
    private var type = 0
    lateinit var billingClient: BillingClient
    var skuDetailsList = arrayListOf<SkuDetails>()
    lateinit var purchasesUpdatedListener: PurchasesUpdatedListener
    lateinit var billingClientStateListener: BillingClientStateListener
    lateinit var acknowledgePurchaseResponseListener: AcknowledgePurchaseResponseListener
    var temPurchase: Purchase? = null

    var connectionNum = 0//连接次数，连接失败时使用
    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0) {
                clientConnection()
            } else if (msg.what == 1) {
                querySku(false)
            }
        }
    }
    var purchaseTime = 0L//购买时间
    override fun initView() {
        binding.apply {
            rl1Month.isSelected = true
            tv1Month.isSelected = true
            tv1MonthPrice.isSelected = true
            tv1MonthPrice99.isSelected = true
            tv1MonthPriceNo.isSelected = true
            tv1MonthPrice299.isSelected = true
            tv1MonthPrice299.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            tv1MonthPrice299.paint.isAntiAlias = true
            tv6MonthPrice399.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            tv6MonthPrice399.paint.isAntiAlias = true
            tv12MonthPrice499.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            tv12MonthPrice499.paint.isAntiAlias = true

            rl1Month.setOnClickListener {
                if (type!=0){
                    type = 0
                    rl1Month.isSelected = true
                    tv1Month.isSelected = true
                    tv1MonthPrice.isSelected = true
                    tv1MonthPrice99.isSelected = true
                    tv1MonthPriceNo.isSelected = true
                    tv1MonthPrice299.isSelected = true
                    rl6Month.isSelected = false
                    tv6Month.isSelected = false
                    tv6MonthPrice.isSelected = false
                    tv6MonthPrice199.isSelected = false
                    tv6MonthPriceNo.isSelected = false
                    tv6MonthPrice399.isSelected = false
                    rl12Month.isSelected = false
                    tv12Month.isSelected = false
                    tv12MonthPrice.isSelected = false
                    tv12MonthPrice299.isSelected = false
                    tv12MonthPriceNo.isSelected = false
                    tv12MonthPrice499.isSelected = false
                }
            }
            rl6Month.setOnClickListener {
                if (type!=1){
                    type = 1
                    rl6Month.isSelected = true
                    tv6Month.isSelected = true
                    tv6MonthPrice.isSelected = true
                    tv6MonthPrice199.isSelected = true
                    tv6MonthPriceNo.isSelected = true
                    tv6MonthPrice399.isSelected = true
                    rl1Month.isSelected = false
                    tv1Month.isSelected = false
                    tv1MonthPrice.isSelected = false
                    tv1MonthPrice99.isSelected = false
                    tv1MonthPriceNo.isSelected = false
                    tv1MonthPrice299.isSelected = false
                    rl12Month.isSelected = false
                    tv12Month.isSelected = false
                    tv12MonthPrice.isSelected = false
                    tv12MonthPrice299.isSelected = false
                    tv12MonthPriceNo.isSelected = false
                    tv12MonthPrice499.isSelected = false
                }
            }
            rl12Month.setOnClickListener {
                if (type!=2){
                    type = 2
                    rl6Month.isSelected = false
                    tv6Month.isSelected = false
                    tv6MonthPrice.isSelected = false
                    tv6MonthPrice199.isSelected = false
                    tv6MonthPriceNo.isSelected = false
                    tv6MonthPrice399.isSelected = false
                    rl1Month.isSelected = false
                    tv1Month.isSelected = false
                    tv1MonthPrice.isSelected = false
                    tv1MonthPrice99.isSelected = false
                    tv1MonthPriceNo.isSelected = false
                    tv1MonthPrice299.isSelected = false
                    rl12Month.isSelected = true
                    tv12Month.isSelected = true
                    tv12MonthPrice.isSelected = true
                    tv12MonthPrice299.isSelected = true
                    tv12MonthPriceNo.isSelected = true
                    tv12MonthPrice499.isSelected = true
                }
            }

        }
    }

    override fun initData() {
        setListener()
        init()
    }
    /*** 初始化 */
    private fun init() {
        BusUtils.register(this)
        //初始化 BillingClient
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        clientConnection()

    }
    /*** 设置监听 */
    private fun setListener() {

        //购买更新的侦听器
        purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                LogUtils.i(
                    "PurchasesUpdatedListener:" + GsonUtils.toJson(billingResult)
                            + "===>" + GsonUtils.toJson(purchases)
                )

                loadingDialog.dismiss()

                //购买结果监听
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
                    var purchased = false
                    //购买成功
                    for (purchase in purchases) {
                        // Verify the purchase.
                        // Ensure entitlement was not already granted for this purchaseToken.
                        // Grant entitlement to the user.

                        //验证购买情况。//确保尚未为此purchaseToken授予权限。//授予用户权限。
                        when (purchase.purchaseState) {
                            Purchase.PurchaseState.PENDING -> handlePurchase(purchase)
                            Purchase.PurchaseState.PURCHASED -> if (purchase.isAcknowledged) {
//                                if (purchase.isAutoRenewing) {
                                purchaseTime = purchase.purchaseTime
                                purchased = true
                                temPurchase = purchase
//                                }
                            } else {
                                handlePurchase(purchase)
                            }
                            else -> {
                                handlePurchase(purchase)
                            }
                        }

                    }

                    if (purchased) {
                        BusUtils.post(BUS_TAG_BUY_STATE_PURCHASED, temPurchase!!)
                        finish()
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    //用户取消购买
                    //处理由用户取消采购流程引起的错误。
                    ToastUtils.showShort("Transaction cancelled")
                    showInterstitialAd(this)
                } else {
                    //处理任何其他错误代码。
                    ToastUtils.showShort(billingResult.debugMessage)
                }
            }

        //连接监听
        billingClientStateListener = object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                loadingDialog.dismiss()
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    connectionNum = 0
                    handler.sendEmptyMessage(1)
                } else {
                    // 需要实现重连机制?
                    reConnect()
                }
            }

            override fun onBillingServiceDisconnected() {
                // 连接断开，实现重连机制
                reConnect()
            }
        }

        //处理操作监听
        acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener {
            loadingDialog.dismiss()
            if (it.responseCode == BillingClient.BillingResponseCode.OK) {
//                ActionHelper.doAction("buy_success")
                LogUtils.i("buy_success")

                //处理消费操作的成功。
                if (temPurchase != null) {
                    BusUtils.post(BUS_TAG_BUY_STATE_PURCHASED, temPurchase!!)
                }
                ToastUtils.showShort("success")
                finish()
            } else {
                ToastUtils.showShort(it.debugMessage)
            }
        }
        binding.btnGet.setOnClickListener {
            buy()
        }
        binding.ivClose.setOnClickListener { finish() }
    }
    /*** 购买 */
    private fun buy() {
        if (connectionNum == 0) {
            subscribe()
        } else {
            ToastUtils.showShort("Google Play not connected.")
            clientConnection()
        }
    }
    /*** 重连 */
    private fun reConnect() {
        if (!isFinishing) {
            when (connectionNum) {
                0, 1 -> {
                    handler.sendEmptyMessageDelayed(0, 1000 * 5)
                }
                2 -> {
                    handler.sendEmptyMessageDelayed(0, 1000 * 60)
                }
                3 -> {
                    handler.sendEmptyMessageDelayed(0, 1000 * 60 * 5)
                }
            }
        }
    }
    /*** 与 Google Play 建立连接 */
    private fun clientConnection() {
        if (!isFinishing) {
            //与 Google Play 建立连接
            loadingDialog.show()
            connectionNum++
            billingClient.startConnection(billingClientStateListener)
        }
    }
    /*** 订阅 */
    private fun subscribe() {
        if (skuDetailsList.isEmpty()) {
            querySku(true)
        } else {

            var vipID =
                when (type) {
                    0 -> Constant.VIP_MONTH
                    1 -> Constant.VIP_HALF_YEAR
                    2 -> Constant.VIP_YEAR

                    else -> ""
                }

            var skuDetails: SkuDetails? = null
            for (temp in skuDetailsList) {
                if (temp.sku == vipID) {
                    skuDetails = temp
                    break
                }
            }

            if (skuDetails == null) {
                ToastUtils.showShort("no options-$vipID")
            } else {
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails!!)
                    .build()
                val responseCode =
                    billingClient.launchBillingFlow(this, flowParams).responseCode
            }
        }
    }

    /*** 查询SKU */
    private fun querySku(doSub: Boolean) {
        var result = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        loadingDialog.show()
        //查询可供购买的商品
        //通过调用querySkuDetailsAsync（）检索“skuDetails”的值。
        val skuList: MutableList<String> = ArrayList()
        skuList.add(Constant.VIP_MONTH)
        skuList.add(Constant.VIP_HALF_YEAR)
        skuList.add(Constant.VIP_YEAR)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
        billingClient.querySkuDetailsAsync(params.build(),
            SkuDetailsResponseListener { _, skuDetailsList ->
                loadingDialog.dismiss()
                if (skuDetailsList.isNullOrEmpty()) {
                    ToastUtils.showShort("no options")
                } else {
                    this.skuDetailsList.clear()
                    this.skuDetailsList.addAll(skuDetailsList)

                    if (doSub) {
                        subscribe()
                    }
                }
            })
    }

    //处理购买交易
    private fun handlePurchase(purchase: Purchase) {
        purchaseTime = purchase.purchaseTime

        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        loadingDialog.show()
        billingClient.acknowledgePurchase(
            acknowledgePurchaseParams,
            acknowledgePurchaseResponseListener
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
        BusUtils.unregister(this)
    }

    override fun onBackPressed() {
        showInterstitialAd(this) {
            super.onBackPressed()
        }
    }
}