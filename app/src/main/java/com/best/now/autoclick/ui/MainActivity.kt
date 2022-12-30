package com.best.now.autoclick.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.Toast
import com.android.billingclient.api.*
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.BuildConfig
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivityMainBinding
import com.best.now.autoclick.utils.Constant
import com.best.now.autoclick.utils.GPSUtils
import com.best.now.autoclick.utils.adParentList
import com.best.now.autoclick.utils.isPurchased
import com.best.now.autoclick.utils.loadAd
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.permissionx.guolindev.PermissionX

class MainActivity : BaseVMActivity() {
    companion object {
        const val BUS_TAG_UPDATE_PURCHASE_STATE = "update_purchase_state"
        var purchased = true
        var purchaseTime = 0L
        var productId = ""
        const val BUS_TAG_BUY_STATE_PURCHASED = "BUS_TAG_BUY_STATE_PURCHASED"
    }

    lateinit var billingClient: BillingClient
    lateinit var purchasesUpdatedListener: PurchasesUpdatedListener
    lateinit var billingClientStateListener: BillingClientStateListener
    lateinit var acknowledgePurchaseResponseListener: AcknowledgePurchaseResponseListener
    private val binding by binding<ActivityMainBinding>(R.layout.activity_main)
    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                0 -> {
                    clientConnection()
                }
                1 -> {
                    queryPurchases()
                }
                2 -> {
                    BusUtils.post(BUS_TAG_UPDATE_PURCHASE_STATE)
                    updateAdView()
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun initView() {
        binding.apply {

            ivSetting.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }
            PermissionX.init(this@MainActivity)
                .permissions( Manifest.permission.ACCESS_FINE_LOCATION)
                .request { allGranted, _, deniedList ->
                    if (allGranted) {
                        val string = GPSUtils.getInstance().getProvince(this@MainActivity)
                        tvAddress.text = string
                    } else {
                        ToastUtils.showShort("These permissions are denied: $deniedList")
                    }
                }
            ivTher.setOnClickListener {
                 if (isPurchased(this@MainActivity)){
                    PermissionX.init(this@MainActivity)
                        .permissions( Manifest.permission.ACCESS_FINE_LOCATION)
                        .request { allGranted, _, deniedList ->
                            if (allGranted) {
                                val locations = GPSUtils.getInstance().getLocation(this@MainActivity)
                                WebPlayActivity.startActivity(
                                    this@MainActivity,
                                    "Thermometer",
                                    Constant.URL_TEMPERATURE+"?lon=${locations[0]}&lat=${locations[1]}"
                                )
                            } else {
                                ToastUtils.showShort("These permissions are denied: $deniedList")
                            }
                        }
                }
            }
            ivHyg.setOnClickListener {
                if (isPurchased(this@MainActivity)){
                    PermissionX.init(this@MainActivity)
                        .permissions( Manifest.permission.ACCESS_FINE_LOCATION)
                        .request { allGranted, _, deniedList ->
                            if (allGranted) {
                                val locations = GPSUtils.getInstance().getLocation(this@MainActivity)
                                WebPlayActivity.startActivity(
                                    this@MainActivity,
                                    "Hygrometer",
                                    Constant.URL_HUMIDITY+"?lon=${locations[0]}&lat=${locations[1]}"
                                )
                            } else {
                                ToastUtils.showShort("These permissions are denied: $deniedList")
                            }
                        }
                }
            }
            ivAir.setOnClickListener {
                if (isPurchased(this@MainActivity)){
                    PermissionX.init(this@MainActivity)
                        .permissions( Manifest.permission.ACCESS_FINE_LOCATION)
                        .request { allGranted, _, deniedList ->
                            if (allGranted) {
                                val locations = GPSUtils.getInstance().getLocation(this@MainActivity)
                                WebPlayActivity.startActivity(
                                    this@MainActivity,
                                    "air quality",
                                    Constant.URL_QUALITY+"?lon=${locations[0]}&lat=${locations[1]}"
                                )
                            } else {
                                ToastUtils.showShort("These permissions are denied: $deniedList")
                            }
                        }
                }
            }

        }
    }




    override fun initData() {
        setListener()
        //初始化 BillingClient
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        clientConnection()
        loadAd(binding.advBanner)
    }

    private fun setListener() {

        //购买更新的侦听器
        purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                //购买结果监听
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
                    //购买成功
                    for (purchase in purchases) {

                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    //用户取消购买
                    //处理由用户取消采购流程引起的错误。
                    Toast.makeText(this, "Transaction cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    //处理任何其他错误代码。
                    Toast.makeText(this, billingResult.debugMessage, Toast.LENGTH_SHORT).show()
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
                    reConnect()
                    // 需要实现重连机制?
                }
            }

            override fun onBillingServiceDisconnected() {
                // 连接断开，实现重连机制
                reConnect()
            }
        }

        //处理操作监听
        acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener {
            if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                LogUtils.i("buy_success")
                purchased = true
                //处理消费操作的成功。
                handler.sendEmptyMessage(2)
            }
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

    override fun onResume() {
        super.onResume()
        queryPurchases()
    }

    var connectionNum = 0//连接次数，连接失败时使用

    /*** 查询购买交易 */
    private fun queryPurchases() {
        if (connectionNum != 0) {
            clientConnection()
            return
        }

        loadingDialog.show()
        //查询购买交易
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS) { billingResult, purchases ->
            loadingDialog.dismiss()
            //购买结果监听
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchased = false
                purchaseTime = 0
                productId = ""
                if (purchases.isNullOrEmpty()) {
                    if (intent.getBooleanExtra("TurnToSell", false)) {
                        intent.removeExtra("TurnToSell")
                        startActivity(Intent(this, SubscribeActivity::class.java))
                    }
                } else {
                    for (purchase in purchases) {
                        //验证购买情况。//确保尚未为此purchaseToken授予权限。//授予用户权限。
                        when (purchase.purchaseState) {
                            Purchase.PurchaseState.PENDING -> handlePurchase(purchase)
                            Purchase.PurchaseState.PURCHASED -> if (purchase.isAcknowledged) {
                                purchased = true
                                purchaseTime = purchase.purchaseTime
                                productId = GsonUtils.toJson(purchase.skus)
                            } else {
                                handlePurchase(purchase)
                            }
                            else -> {
                                handlePurchase(purchase)
                            }
                        }
                    }
                }

                handler.sendEmptyMessage(2)

            } else if (intent.getBooleanExtra("TurnToSell", false)) {
                intent.removeExtra("TurnToSell")
                startActivity(Intent(this, SubscribeActivity::class.java))
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

    /*** 更新广告控件 */
    fun updateAdView() {
        for (adParentView in adParentList) {
            if (adParentView.isAttachedToWindow) {
                adParentView.visibility = View.VISIBLE
            }
        }
    }

    //处理购买交易
    private fun handlePurchase(purchase: Purchase) {
        purchaseTime = purchase.purchaseTime
        productId = GsonUtils.toJson(purchase.skus)

        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(
            acknowledgePurchaseParams,
            acknowledgePurchaseResponseListener
        )
    }

    @BusUtils.Bus(tag = BUS_TAG_BUY_STATE_PURCHASED)
    fun purchase(purchase: Purchase) {
        purchased = true
        purchaseTime = purchase.purchaseTime
        productId = GsonUtils.toJson(purchase.skus)
        handler.sendEmptyMessage(2)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2001) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { // 用户成功授予权限

            } else {
//                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
//                    ToastUtils.showShort("请到设置中同意存储权限")
//                } else {
//                    ToastUtils.showShort("请务必同意存储权限")
//                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        BusUtils.register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        BusUtils.unregister(this)
        billingClient?.endConnection()
    }
}