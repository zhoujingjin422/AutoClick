package com.best.now.autoclick.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.android.billingclient.api.*
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.TimePicker
import com.best.now.autoclick.WorkService
import com.best.now.autoclick.databinding.ActivityMainBinding
import com.best.now.autoclick.databinding.LayoutAccessBinding
import com.best.now.autoclick.databinding.LayoutModelMultiBinding
import com.best.now.autoclick.databinding.LayoutModelSingleBinding
import com.best.now.autoclick.ext.getSpValue
import com.best.now.autoclick.ext.getTimeFormat
import com.best.now.autoclick.ext.putSpValue
import com.best.now.autoclick.utils.adParentList
import com.best.now.autoclick.utils.isPurchased
import com.best.now.autoclick.utils.isServiceON
import com.best.now.autoclick.utils.loadAd
import com.best.now.autoclick.view.WorkSetting
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

class MainActivity : BaseVMActivity() {
    companion object {
        const val BUS_TAG_UPDATE_PURCHASE_STATE = "update_purchase_state"
        var purchased = false
        var purchaseTime = 0L
        var productId = ""
        const val BUS_TAG_BUY_STATE_PURCHASED = "BUS_TAG_BUY_STATE_PURCHASED"
        const val STOP_WORK = "stop_work"
        const val SINGLEMODEL = "single"
        const val MULTIMODEL = "multi"
        const val DISABLEMODEL = "disable"
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
                    binding.ivSettingMulti.isEnabled = true
                    binding.ivSettingSingle.isEnabled = true
                    binding.ivCommonSetting.isEnabled = true
                    binding.ivSettingSingle.setTextColor(resources.getColor(R.color.c_616780))
                    binding.ivSettingMulti.setTextColor(resources.getColor(R.color.c_616780))

                    updateAdView()
                }
                3 -> {
                    binding.btnMulEnable.setBackgroundResource(R.drawable.shape_button_click)
                    binding.btnMulEnable.setTextColor(resources.getColor(R.color.white))
                    binding.btnMulEnable.text = "ENABLE"
                    binding.ivSettingMulti.isEnabled = true
                    binding.ivSettingSingle.isEnabled = true
                    binding.ivCommonSetting.isEnabled = true
                    binding.ivSettingSingle.setTextColor(resources.getColor(R.color.c_616780))
                    binding.ivSettingMulti.setTextColor(resources.getColor(R.color.c_616780))
                    binding.btnSingleEnable.setBackgroundResource(R.drawable.shape_button_click)
                    binding.btnSingleEnable.setTextColor(resources.getColor(R.color.white))
                    binding.btnSingleEnable.text = "ENABLE"
                    modelNow = DISABLEMODEL
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun initView() {
        binding.apply {
            btnSingleEnable.setOnClickListener {
                if (modelNow != SINGLEMODEL) {
                    startWorkService(SINGLEMODEL, btnSingleEnable)
                } else startWorkService(DISABLEMODEL, btnSingleEnable)

            }
            ivCommonSetting.setOnClickListener {
                startActivity(Intent(this@MainActivity,CommonSettingActivity::class.java))
            }
            btnMulEnable.setOnClickListener {
                if (modelNow != MULTIMODEL) {
                    startWorkService(MULTIMODEL, btnMulEnable)
                } else startWorkService(DISABLEMODEL, btnMulEnable)
            }
            llSingleInstruction.setOnClickListener {
                startActivity(Intent(this@MainActivity, SingleInstructionActivity::class.java))
            }
            llMultiInstruction.setOnClickListener {
                startActivity(Intent(this@MainActivity, MultiInstructionActivity::class.java))
            }
            ivSetting.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }
            ivSettingSingle.setOnClickListener {
                //???????????????????????????????????????
                    showSettingSingleDialog()
            }
            ivSettingMulti.setOnClickListener {
                //???????????????????????????????????????
                    showSettingMultiDialog()
            }
        }
    }

    private var dialogMulti: AlertDialog? = null
    private fun showSettingMultiDialog() {
        dialogMulti = AlertDialog.Builder(this).apply {
            val view =
                LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_model_multi, null)
            setView(view)
            val binding = DataBindingUtil.bind<LayoutModelMultiBinding>(view)
            var setting = getSpValue("multi", WorkSetting())
            binding?.apply {
                    etInput.setText(setting.click_interval.toString())
                etInputDelay.setText(setting.action_delay.toString())
                etInputSwipe.setText(setting.swipe_duration.toString())
                listTypeDelay.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        p2: Int,
                        p3: Long
                    ) {
                        setting.uint_action_delay = when (p2) {
                            0 -> 1
                            1 -> 1000
                            else -> 60 * 1000
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }
                listTypeSwipe.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        p2: Int,
                        p3: Long
                    ) {
                        setting.uint_swipe_interval = when (p2) {
                            0 -> 1
                            1 -> 1000
                            else -> 60 * 1000
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }
                }
                listType.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        p2: Int,
                        p3: Long
                    ) {
                        setting.uint_click_interval = when (p2) {
                            0 -> 1
                            1 -> 1000
                            else -> 60 * 1000
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }
                }
                when (setting.uint_action_delay) {
                    1 -> listTypeDelay.setSelection(0)
                    1000 -> listTypeDelay.setSelection(1)
                    else -> listTypeDelay.setSelection(2)
                }
                when (setting.uint_swipe_interval) {
                    1 -> listTypeSwipe.setSelection(0)
                    1000 -> listTypeSwipe.setSelection(1)
                    else -> listTypeSwipe.setSelection(2)
                }
                when (setting.uint_click_interval) {
                    1 -> listType.setSelection(0)
                    1000 -> listType.setSelection(1)
                    else -> listType.setSelection(2)
                }
                when (setting.stop_model) {
                    0 -> rbOne.isChecked = true
                    1 -> rbSecond.isChecked = true
                    2 -> rbThird.isChecked = true
                }
                tvTimeAll.text = setting.count_down.getTimeFormat()
                tvTimeAll.tag = setting.count_down
                etCountNum.setText(setting.circle_count.toString())
                rbOne.setOnClickListener {
                    rbOne.isChecked = true
                    rbSecond.isChecked = false
                    rbThird.isChecked = false
                }
                rbSecond.setOnClickListener {
                    rbOne.isChecked = false
                    rbSecond.isChecked = true
                    rbThird.isChecked = false
                }
                rbThird.setOnClickListener {
                    rbOne.isChecked = false
                    rbSecond.isChecked = false
                    rbThird.isChecked = true
                }
                btnCancel.setOnClickListener {
                    dialogMulti?.dismiss()
                }
                btnSave.setOnClickListener {
                    if (rbOne.isChecked)
                        setting.stop_model = 0
                    if (rbSecond.isChecked)
                        setting.stop_model = 1
                    if (rbThird.isChecked)
                        setting.stop_model = 2
                    setting.circle_count = etCountNum.text.toString().toInt()
                    setting.click_interval = etInput.text.toString().toLong()
                    setting.swipe_duration = etInputSwipe.text.toString().toLong()
                    setting.action_delay = etInputDelay.text.toString().toLong()
                    setting.count_down = tvTimeAll.tag as Int
                    putSpValue("multi", setting)
                    dialogMulti?.dismiss()
                }
                tvTimeAll.setOnClickListener {
                        val timePicker = TimePicker(
                            context,
                            tvTimeAll.tag as Int,
                            object : TimePicker.TimeSetListener {
                                override fun onSaveTime(time: Int) {
                                    tvTimeAll.text = time.getTimeFormat()
                                    tvTimeAll.tag = time
                                }
                            })
                    timePicker.show()
                }

            }
        }.create()
        dialogMulti?.show()
    }

    private var singleDialog: AlertDialog? = null
    private fun showSettingSingleDialog() {
        singleDialog = AlertDialog.Builder(this).apply {
            val view =
                LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_model_single, null)
            setView(view)
            val binding = DataBindingUtil.bind<LayoutModelSingleBinding>(view)
            var setting = getSpValue("single", WorkSetting())
            binding?.apply {
                etInput.setText(setting.click_interval.toString())
                when (setting.uint_click_interval) {
                    1 -> listType.setSelection(0)
                    1000 -> listType.setSelection(1)
                    else -> listType.setSelection(2)
                }
                when (setting.stop_model) {
                    0 -> rbOne.isChecked = true
                    1 -> rbSecond.isChecked = true
                    2 -> rbThird.isChecked = true
                }
                listType.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        p2: Int,
                        p3: Long
                    ) {
                        setting.uint_click_interval = when (p2) {
                            0 -> 1
                            1 -> 1000
                            else -> 60 * 1000
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }
                tvTimeAll.text = setting.count_down.getTimeFormat()
                tvTimeAll.tag = setting.count_down
                etCountNum.setText(setting.circle_count.toString())
                rbOne.setOnClickListener {
                    rbOne.isChecked = true
                    rbSecond.isChecked = false
                    rbThird.isChecked = false
                }
                rbSecond.setOnClickListener {
                    rbOne.isChecked = false
                    rbSecond.isChecked = true
                    rbThird.isChecked = false
                }
                rbThird.setOnClickListener {
                    rbOne.isChecked = false
                    rbSecond.isChecked = false
                    rbThird.isChecked = true
                }
                btnCancel.setOnClickListener {
                    singleDialog?.dismiss()
                }
                etCountNum.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun afterTextChanged(p0: Editable?) {
                        p0?.toString()
                    }
                })
                btnSave.setOnClickListener {
                    if (rbOne.isChecked)
                        setting.stop_model = 0
                    if (rbSecond.isChecked)
                        setting.stop_model = 1
                    if (rbThird.isChecked)
                        setting.stop_model = 2
                    setting.circle_count = etCountNum.text.toString().toInt()
                    setting.click_interval = etInput.text.toString().toLong()
                    setting.count_down = tvTimeAll.tag as Int
                    putSpValue("single", setting)
                    singleDialog?.dismiss()
                }
                tvTimeAll.setOnClickListener {
                       val timePicker = TimePicker(
                            context,
                           tvTimeAll.tag as Int,
                            object : TimePicker.TimeSetListener {
                                override fun onSaveTime(time: Int) {
                                    tvTimeAll.text = time.getTimeFormat()
                                    tvTimeAll.tag = time
                                }
                            })
                    timePicker.show()
                }
            }
        }.create()
        singleDialog?.show()
    }

    private var modelNow: String = ""
    private fun startWorkService(mode: String, btn: Button) {
        if (isPurchased(this@MainActivity)) {
            //?????????????????????
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    if (isServiceON(this@MainActivity, "com.best.now.autoclick.WorkService")) {
                        modelNow = mode
                        startService(Intent(this@MainActivity, WorkService::class.java).apply {
                            action = mode
                        })
                        if (modelNow == DISABLEMODEL) {
                            btn.setBackgroundResource(R.drawable.shape_button_click)
                            btn.setTextColor(resources.getColor(R.color.white))
                            binding.ivSettingMulti.isEnabled = true
                            binding.ivSettingSingle.isEnabled = true
                            binding.ivSettingSingle.setTextColor(resources.getColor(R.color.c_616780))
                            binding.ivSettingMulti.setTextColor(resources.getColor(R.color.c_616780))
                            binding.ivCommonSetting.isEnabled = true
                            btn.text = "ENABLE"
                        } else {
                            btn.setBackgroundResource(R.drawable.shape_button_disable)
                            btn.setTextColor(resources.getColor(R.color.c_eff2fe))
                            binding.ivCommonSetting.isEnabled = false
                            if (MULTIMODEL == mode){
                                binding.ivSettingMulti.isEnabled = false
                                binding.ivSettingMulti.setTextColor(resources.getColor(R.color.disable_color))
                            }
                            else{
                                binding.ivSettingSingle.isEnabled = false
                                binding.ivSettingSingle.setTextColor(resources.getColor(R.color.disable_color))
                            }
                            btn.text = "DISABLE"
                        }
                    } else {
                        showAccessDialog()
                    }
                } else {
                    AlertDialog.Builder(this).apply {
                        setTitle("Tips")
                        setMessage("This function requires floating window permission")
                        setPositiveButton(
                            "OK"
                        ) { _, _ ->
                            try {
                                startActivityForResult(
                                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION),
                                    2001
                                )
                            } catch (e: Exception) {
                                startActivity(Intent(Settings.ACTION_SETTINGS))
                                e.printStackTrace()
                            }
                        }
                    }.create().show()

                }
            } else {
                startService(Intent(this@MainActivity, WorkService::class.java).apply {
                    action = mode
                })
            }
        }

    }

    private var dialog: AlertDialog? = null

    /**
     * ????????????????????????
     */
    private fun showAccessDialog() {
        if (dialog == null) {
            dialog = AlertDialog.Builder(this).apply {
                val view =
                    LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_access, null)
                setView(view)
                val bind = DataBindingUtil.bind<LayoutAccessBinding>(view)
                val text = "<font color='#616780' opacity:0.5>Please tap on </font>" +
                        "<font color='#616780' size='45px' opacity:0.7>OK</font>" +
                        "<font color='#616780' opacity:0.5>, then choose </font>" +
                        "<font color='#616780' size='45px' opacity:0.7>Auto Clicker </font>" +
                        "<font color='#616780' opacity:0.5>in the list and active the service.</font>"
                bind?.tvChangeColor?.text = Html.fromHtml(text)
                bind?.tvWatch?.setOnClickListener {
                    startActivity(Intent(this@MainActivity, TutorialActivity::class.java))
                }
                bind?.btnOk?.setOnClickListener {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                    dialog?.dismiss()
                }
            }.create()
        }
        dialog?.show()
    }

    override fun initData() {
        setListener()
        //????????? BillingClient
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        clientConnection()
        loadAd(binding.advBanner)
    }

    private fun setListener() {

        //????????????????????????
        purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                //??????????????????
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
                    //????????????
                    for (purchase in purchases) {

                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    //??????????????????
                    //???????????????????????????????????????????????????
                    Toast.makeText(this, "Transaction cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    //?????????????????????????????????
                    Toast.makeText(this, billingResult.debugMessage, Toast.LENGTH_SHORT).show()
                }
            }

        //????????????
        billingClientStateListener = object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                loadingDialog.dismiss()
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    connectionNum = 0
                    handler.sendEmptyMessage(1)
                } else {
                    reConnect()
                    // ?????????????????????????
                }
            }

            override fun onBillingServiceDisconnected() {
                // ?????????????????????????????????
                reConnect()
            }
        }

        //??????????????????
        acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener {
            if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                LogUtils.i("buy_success")
                purchased = true
                //??????????????????????????????
                handler.sendEmptyMessage(2)
            }
        }

    }

    /*** ?????? */
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

    var connectionNum = 0//????????????????????????????????????

    /*** ?????????????????? */
    private fun queryPurchases() {
        if (connectionNum != 0) {
            clientConnection()
            return
        }

        loadingDialog.show()
        //??????????????????
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS) { billingResult, purchases ->
            loadingDialog.dismiss()
            //??????????????????
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
                        //?????????????????????//??????????????????purchaseToken???????????????//?????????????????????
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

    /*** ??? Google Play ???????????? */
    private fun clientConnection() {
        if (!isFinishing) {
            //??? Google Play ????????????
            loadingDialog.show()
            connectionNum++
            billingClient.startConnection(billingClientStateListener)
        }
    }

    /*** ?????????????????? */
    fun updateAdView() {
        for (adParentView in adParentList) {
            if (adParentView.isAttachedToWindow) {
                adParentView.visibility = View.VISIBLE
            }
        }
    }

    //??????????????????
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

    @BusUtils.Bus(tag = STOP_WORK)
    fun purchase() {
        handler.sendEmptyMessage(3)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2001) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { // ????????????????????????

            } else {
//                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
//                    ToastUtils.showShort("?????????????????????????????????")
//                } else {
//                    ToastUtils.showShort("???????????????????????????")
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