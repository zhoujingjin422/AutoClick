package com.best.now.autoclick.view

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GestureResultCallback
import android.accessibilityservice.GestureDescription
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.best.now.autoclick.R
import com.best.now.autoclick.TimePicker
import com.best.now.autoclick.WorkService
import com.best.now.autoclick.databinding.LayoutDrugViewBinding
import com.best.now.autoclick.databinding.LayoutModelMultiBinding
import com.best.now.autoclick.databinding.LayoutModelSingleBinding
import com.best.now.autoclick.ext.dp
import com.best.now.autoclick.ext.getSpValue
import com.best.now.autoclick.ext.getTimeFormat
import com.best.now.autoclick.ext.putSpValue
import com.best.now.autoclick.ui.MainActivity
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import java.util.*
import kotlin.math.abs

/**
author:zhoujingjin
date:2022/11/21
 */
class MenuView(
    private val context: Context,
    accessibilityService: AccessibilityService,
    private val windowManager: WindowManager,
    private val singleMode: Boolean = true
) : View.OnTouchListener {
    private var params: WindowManager.LayoutParams
    private val arr = IntArray(2)
    private var rawX = 0f
    private var rawY = 0f
    private var isSliding = false
    private var actionList = mutableListOf<BaseAutoClick>()
    private val MESSAGE_WHAT_STOP = 1
    private val MESSAGE_WHAT_RUN = 2
    private var taskPositionNow = 0
    private var isRunningTak = false
    private var viewLayout :View
    private var setting = context.getSpValue(if (singleMode) "single" else "multi", WorkSetting())
    private var circleCount = -1
    private var circleCountNow = 0
    private var isShow = true
    private var ivAdd :ImageView
    private var ivMinus :ImageView
    private var ivSwipe :ImageView
    private var ivDrug :ImageView
    private lateinit var ivPlay :ImageView
    private var ivSetting :ImageView
    private  val handler: Handler = object : Handler() {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_WHAT_STOP -> {
                    ivPlay?.setImageResource(R.mipmap.icon_play)
                }
                MESSAGE_WHAT_RUN -> {
                    if (!isRunningTak)
                        return
                    ivPlay?.setImageResource(R.mipmap.icon_pause)
                    if (taskPositionNow <= actionList.size - 1) {
                        val gestureDescription = actionList[taskPositionNow].getGestureDescription()
                        accessibilityService.dispatchGesture(
                            gestureDescription,
                            object : GestureResultCallback() {
                                override fun onCompleted(gestureDescription: GestureDescription?) {
                                    super.onCompleted(gestureDescription)
                                    LogUtils.e("onCompleted")
                                    taskPositionNow++
                                    nextAction()
                                }

                                override fun onCancelled(gestureDescription: GestureDescription?) {
                                    super.onCancelled(gestureDescription)
                                    LogUtils.e("onCancelled")
                                    nextAction()
                                }
                            },
                            null
                        )
                    }
                }
            }
        }
    }

    private fun nextAction() {
        if (taskPositionNow > actionList.size - 1) {
            when (setting.stop_model) {
                0 -> {
                    taskPositionNow = 0
                    if (singleMode)
                    startTask(setting.click_interval * setting.uint_click_interval)
                    else startTask(setting.action_delay * setting.uint_action_delay)
                }
                1 -> {
                    if (stop) {
                        stopTask()
                    } else {
                        taskPositionNow = 0
                        if (singleMode)
                            startTask(setting.click_interval * setting.uint_click_interval)
                        else startTask(setting.action_delay * setting.uint_action_delay)
                    }
                }
                2 -> {
                    circleCountNow++
                    if (circleCountNow >= circleCount) {
                        stopTask()
                    } else {
                        taskPositionNow = 0
                        if (singleMode)
                            startTask(setting.click_interval * setting.uint_click_interval)
                        else startTask(setting.action_delay * setting.uint_action_delay)
                    }
                }
            }
        } else
            startTask(setting.click_interval * setting.uint_click_interval)
    }
    private fun setViewScale(view: View,start:Float,end:Float){
        val scaleX = ObjectAnimator.ofFloat(view,"scaleX",start,end)
        val scaleY = ObjectAnimator.ofFloat(view,"scaleY",start,end)
        val set = AnimatorSet()
        set.play(scaleX).with(scaleY)
        set.duration = 100
        set.start()
    }
    init {
        val end = context.getSpValue("controllerSize",1f)
        viewLayout = when(context.getSpValue("controllerSize",1f)){
            1f-> LayoutInflater.from(context).inflate(R.layout.layout_drug_view, null)
            0.75f-> LayoutInflater.from(context).inflate(R.layout.layout_drug_view_medium, null)
            else-> LayoutInflater.from(context).inflate(R.layout.layout_drug_view_small, null)
        }
         ivAdd =  viewLayout.findViewById<ImageView>(R.id.iv_add)
         ivMinus =  viewLayout.findViewById<ImageView>(R.id.iv_minus)
         ivSwipe =  viewLayout.findViewById<ImageView>(R.id.iv_swipe)
         ivDrug =  viewLayout.findViewById<ImageView>(R.id.iv_drug)
         ivPlay =  viewLayout.findViewById<ImageView>(R.id.iv_play)
         ivSetting =  viewLayout.findViewById<ImageView>(R.id.iv_setting)
        if (end!=1f){
            setViewScale(ivAdd,1f,end)
            setViewScale(ivMinus,1f,end)
            setViewScale(ivSwipe,1f,end)
            setViewScale(ivDrug,1f,end)
            setViewScale(ivPlay,1f,end)
            setViewScale(ivSetting,1f,end)
        }
            if (singleMode) {
                ivAdd.visibility = View.GONE
                ivMinus.visibility = View.GONE
                ivSwipe.visibility = View.GONE
            }
            ivDrug.setOnTouchListener(this@MenuView)
            ivPlay.setOnClickListener {
                if (isShow) {
                    //????????????
                    if (!isRunningTak) {
                        if (actionList.isNullOrEmpty()) {
                            ToastUtils.showShort("You need add at least one TARGET to can run.")
                            return@setOnClickListener
                        }
                        actionList.forEach {
                            it.updateView(false)
                        }
                        when (setting.stop_model) {
                            //???????????????
                            1 -> startTimeCount()
                            2 -> {
                                circleCountNow = 0
                                circleCount = setting.circle_count
                            }
                        }
                        ivAdd.isEnabled = false
                        ivMinus.isEnabled = false
                        ivSwipe.isEnabled = false
                        ivSetting.isEnabled = false
                        isRunningTak = true
                        startTask(100)
                    } else {
                        stopTask()
                    }
                } else {
                    //????????????????????????view
                    context.startService(Intent(context, WorkService::class.java).apply {
                        action = MainActivity.DISABLEMODEL
                    })
                    BusUtils.post(MainActivity.STOP_WORK)
                }
            }
            ivSetting.setOnClickListener {
                if (!isRunningTak) {
                    if (singleMode) {
                        showSettingSingleDialog()
                    } else showSettingMultiDialog()
                }
            }
            ivAdd.setOnClickListener {
                //???????????????view
                val clickView = DrugPointView(
                    context,
                    windowManager,
                    context.getSpValue("viewSize",60).dp.toInt(),
                    actionList.size + 1
                )
                actionList.add(clickView)
            }
            ivMinus.setOnClickListener {
                //???????????????????????????????????????
                if (actionList.isNotEmpty()) {
                    actionList[actionList.size - 1].removeActionView()
                    actionList.removeAt(actionList.size - 1)
                }
            }
            ivSwipe.setOnClickListener {
                //???????????????????????????
                val slideView = SlideView(
                    context,
                    windowManager,
                    actionList.size + 1,
                    setting.swipe_duration * setting.uint_swipe_interval
                )
                actionList.add(slideView)
            }
        if (singleMode) {
            val clickView = DrugPointView(
                context,
                windowManager,
                context.getSpValue("viewSize",60).dp.toInt(),
                0
            )
            actionList.add(clickView)
        }
        params = setParams()
        params.x = 0
        params.y = 100
        windowManager.addView(viewLayout, params)
    }
    //???????????????
    private var stop = false
    private var timer: Timer? = null
    private fun startTimeCount() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                stop = true
                timer?.cancel()
                timer = null
            }
        }, setting.count_down * 1000L, setting.count_down * 1000L)
    }

    private fun startTask(delay: Long = 0) {
        val obtain = Message.obtain()
        obtain.what = MESSAGE_WHAT_RUN
        handler.sendMessageDelayed(obtain, delay)
    }

    private fun stopTask() {
        actionList.forEach {
            it.updateView(true)
        }
        isRunningTak = false
        stop = false
        taskPositionNow = 0
        circleCountNow = 0
        timer?.cancel()
        timer = null
        val obtain = Message.obtain()
        obtain.what = MESSAGE_WHAT_STOP
        handler.sendMessage(obtain)
        ivAdd.isEnabled = true
        ivMinus.isEnabled = true
        ivSwipe.isEnabled = true
        ivSetting.isEnabled = true
    }

    private fun setParams(): WindowManager.LayoutParams {
        val windowLayoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowLayoutParams.type =
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY //??????????????????????????????
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                windowLayoutParams.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            windowLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
        } else {
            windowLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        windowLayoutParams.flags =
            windowLayoutParams.flags or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        //        layoutParams.packageName = getPackageName();
        windowLayoutParams.flags =
            windowLayoutParams.flags or (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)

        windowLayoutParams.format = PixelFormat.TRANSPARENT
        windowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        windowLayoutParams.gravity = Gravity.TOP or Gravity.START
        return windowLayoutParams
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        when (motionEvent?.action) {
            MotionEvent.ACTION_DOWN -> {
                viewLayout.getLocationOnScreen(arr)
                rawX = motionEvent.rawX
                rawY = motionEvent.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val xNow = motionEvent.rawX
                val yNow = motionEvent.rawY
                val absX = abs(xNow - rawX)
                val absY = abs(yNow - rawY)
                if (absX > 15f || absY > 15f) {
                    params.x = arr[0] + (xNow - rawX).toInt()
                    params.y = arr[1] + (yNow - rawY).toInt()
                    windowManager.updateViewLayout(viewLayout, params)
                }
            }
            MotionEvent.ACTION_UP -> {
                val xNow = motionEvent.rawX
                val yNow = motionEvent.rawY
                val absX = abs(xNow - rawX)
                val absY = abs(yNow - rawY)
                if (absX > 15f || absY > 15f) {
                    //??????
                    isSliding = true
                } else {
                    //???????????????????????????
                    if (isShow) {
                        isShow = false
                        ivPlay.setImageResource(R.mipmap.icon_close_menu)
                        ivAdd.visibility = View.GONE
                        ivMinus.visibility = View.GONE
                        ivSetting.visibility = View.GONE
                        ivSwipe.visibility = View.GONE
                        //??????????????????????????????
                        actionList.forEach {
                            it.setViewVisibility(View.INVISIBLE)
                        }
                    } else {
                        isShow = true
                        if (isRunningTak) {
                            ivPlay.setImageResource(R.mipmap.icon_pause)
                        } else {
                            ivPlay.setImageResource(R.mipmap.icon_play)
                        }
                        ivSetting.visibility = View.VISIBLE
                        if (!singleMode) {
                            ivMinus.visibility = View.VISIBLE
                            ivSwipe.visibility = View.VISIBLE
                            ivAdd.visibility = View.VISIBLE
                        }
                        actionList.forEach {
                            it.setViewVisibility(View.VISIBLE)
                        }
                    }
                }
            }
        }
        return true
    }

    fun clearView() {
        isRunningTak = false
        windowManager.removeView(viewLayout)
        actionList.forEach {
            it.removeActionView()
        }
    }

    private var dialogMulti: AlertDialog? = null
    private fun showSettingMultiDialog() {
        dialogMulti = AlertDialog.Builder(context, R.style.alertDialogStyle).apply {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_model_multi, null)
            setView(view)
            val binding = DataBindingUtil.bind<LayoutModelMultiBinding>(view)
            var setting = context.getSpValue("multi", WorkSetting())
            binding?.apply {
                etInput.setText(setting.click_interval.toString())
                etInputDelay.setText(setting.action_delay.toString())
                etInputSwipe.setText(setting.swipe_duration.toString())
                listTypeDelay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                listTypeSwipe.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                listType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                    context.putSpValue("multi", setting)
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
                            },true)
                    timePicker.show()
                }

            }
        }.create()
        dialogMulti?.window?.setType(2032)
        dialogMulti?.show()
    }

    private var singleDialog: AlertDialog? = null
    private fun showSettingSingleDialog() {
        singleDialog = AlertDialog.Builder(context, R.style.alertDialogStyle).apply {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_model_single, null)
            setView(view)
            val binding = DataBindingUtil.bind<LayoutModelSingleBinding>(view)
            var setting = context.getSpValue("single", WorkSetting())
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
                listType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                        TODO("Not yet implemented")
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
                    this@MenuView.setting = setting
                    context.putSpValue("single", setting)
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
                            },
                            true
                        )
                     timePicker.show()
                }
            }
        }.create()
        singleDialog?.window?.setType(2032)
        singleDialog?.show()
    }
}