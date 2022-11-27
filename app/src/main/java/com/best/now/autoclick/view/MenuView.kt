package com.best.now.autoclick.view

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GestureResultCallback
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.best.now.autoclick.R
import com.best.now.autoclick.TimePicker
import com.best.now.autoclick.databinding.LayoutDrugViewBinding
import com.best.now.autoclick.databinding.LayoutModelMultiBinding
import com.best.now.autoclick.databinding.LayoutModelSingleBinding
import com.best.now.autoclick.ext.dp
import com.best.now.autoclick.ext.getSpValue
import com.best.now.autoclick.ext.getTimeFormat
import com.best.now.autoclick.ext.putSpValue
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import java.util.*
import kotlin.math.abs

/**
author:zhoujingjin
date:2022/11/21
 */
class MenuView(private val context:Context,accessibilityService: AccessibilityService,private val windowManager: WindowManager,singleMode:Boolean = true):View.OnTouchListener {
    private  var binding: LayoutDrugViewBinding? = null
    private var params:WindowManager.LayoutParams
    private val arr = IntArray(2)
    private var rawX = 0f
    private var rawY = 0f
    private var isSliding = false
    private var actionList = mutableListOf<BaseAutoClick>()
    private val MESSAGE_WHAT_STOP = 1
    private val MESSAGE_WHAT_RUN = 2
    private var taskPositionNow = 0
    private var isRunningTak = false
    private val viewLayout =  LayoutInflater.from(context).inflate(R.layout.layout_drug_view,null)
    private var setting = context.getSpValue(if(singleMode) "single" else "multi",WorkSetting())
    private var circleCount= -1
    private var circleCountNow= 0
    private val handler: Handler = object : Handler() {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_WHAT_STOP -> {
                    binding?.ivPlay?.setImageResource(R.mipmap.icon_play)
                }
                MESSAGE_WHAT_RUN -> {
                    if (!isRunningTak)
                        return
                    binding?.ivPlay?.setImageResource(R.mipmap.icon_close)
                    if (taskPositionNow<=actionList.size-1){
                        val gestureDescription = actionList[taskPositionNow].getGestureDescription()
                         accessibilityService.dispatchGesture(gestureDescription,object : GestureResultCallback() {
                            override fun onCompleted(gestureDescription: GestureDescription?) {
                                super.onCompleted(gestureDescription)
                                taskPositionNow++
                                if (taskPositionNow>actionList.size-1){
                                    when (setting.stop_model){
                                        0->{
                                            taskPositionNow = 0
                                            startTask(setting.click_interval*setting.uint_click_interval)
                                        }
                                        1->{
                                            if (stop){
                                                stopTask()
                                            }else{
                                                taskPositionNow = 0
                                                startTask(setting.click_interval*setting.uint_click_interval)
                                            }
                                        }
                                        2->{
                                            circleCountNow++
                                            if (circleCountNow>=circleCount){
                                                stopTask()
                                            }else{
                                                taskPositionNow = 0
                                                startTask(setting.click_interval*setting.uint_click_interval)
                                            }
                                        }
                                    }
                                }else
                                    startTask(setting.click_interval*setting.uint_click_interval)
                            }

                            override fun onCancelled(gestureDescription: GestureDescription?) {
                                super.onCancelled(gestureDescription)
                                LogUtils.e("onCancelled")
                            }
                        },null)
                    }
                }
            }
        }
    }
    init {
        binding =  DataBindingUtil.bind(viewLayout)
        binding?.apply {
            if (singleMode){
                ivAdd.visibility = View.GONE
                ivMinus.visibility = View.GONE
                ivSwipe.visibility = View.GONE
            }
            ivDrug.setOnTouchListener(this@MenuView)
            ivPlay.setOnClickListener {
                //执行任务
                if (!isRunningTak){
                    if (actionList.isNullOrEmpty()){
                        ToastUtils.showShort("You need add at least one TARGET to can run.")
                        return@setOnClickListener
                    }
                    actionList.forEach {
                        it.updateView(false)
                    }
                    when (setting.stop_model){
                        //停止的类型
                        1-> startTimeCount()
                        2-> {
                            circleCountNow = -1
                            circleCount = setting.circle_count
                        }
                    }
                    binding?.ivAdd?.isEnabled = false
                    binding?.ivMinus?.isEnabled = false
                    binding?.ivSwipe?.isEnabled = false
                    binding?.ivSetting?.isEnabled = false
                    startTask(100)
                }else{
                    stopTask()
                }
            }
            ivSetting.setOnClickListener {
                if (!isRunningTak){
                    if (singleMode){
                        showSettingSingleDialog()
                    }else showSettingMultiDialog()
                }
            }
            ivAdd.setOnClickListener {
                //添加点击的view
               val clickView =  DrugPointView(context,windowManager,60.dp.toInt(),14.dp.toInt(),actionList.size+1)
                actionList.add(clickView)
            }
            ivMinus.setOnClickListener {
                //删除最近一次添加的一个任务
                if (actionList.isNotEmpty()){
                    actionList[actionList.size-1].removeActionView()
                    actionList.removeAt(actionList.size-1)
                }
            }
            ivSwipe.setOnClickListener {
                //添加一个滑动的任务
                val slideView = SlideView(context,windowManager,actionList.size+1,setting.swipe_duration*setting.uint_swipe_interval)
                actionList.add(slideView)
            }
        }
        if (singleMode){
            val clickView =  DrugPointView(
                context,
                windowManager,
                60.dp.toInt(),
                14.dp.toInt(),
                0
            )
            actionList.add(clickView)
        }
        params = setParams()
        params.x = 0
        params.y = 100
        windowManager.addView(viewLayout,params)
    }

    //开启计时器
    private var stop = false
    private  var timer: Timer? = null
    private fun startTimeCount() {
         timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                stop = true
            }
        },0,setting.count_down*1000L)
    }

    private fun startTask(delay:Long = 0) {
        isRunningTak = true
        val obtain = Message.obtain()
        obtain.what = MESSAGE_WHAT_RUN
        handler.sendMessageDelayed(obtain,delay)
    }
    private fun stopTask() {
        actionList.forEach {
            it.updateView(true)
        }
        isRunningTak = false
        taskPositionNow = 0
        circleCountNow = -1
        timer?.cancel()
        val obtain = Message.obtain()
        obtain.what = MESSAGE_WHAT_STOP
        handler.sendMessage(obtain)
        binding?.ivAdd?.isEnabled = true
        binding?.ivMinus?.isEnabled = true
        binding?.ivSwipe?.isEnabled = true
        binding?.ivSetting?.isEnabled = true
    }

    private fun setParams(): WindowManager.LayoutParams {
        val   windowLayoutParams =  WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY //刘海屏延伸到刘海里面
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                windowLayoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            windowLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
        } else {
            windowLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        windowLayoutParams.flags = windowLayoutParams.flags or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        //        layoutParams.packageName = getPackageName();
        windowLayoutParams.flags = windowLayoutParams.flags or (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)

        windowLayoutParams.format = PixelFormat.TRANSPARENT
        windowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        windowLayoutParams.gravity = Gravity.TOP or Gravity.START
        return windowLayoutParams
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        when(motionEvent?.action){
            MotionEvent.ACTION_DOWN->{
                viewLayout.getLocationOnScreen(arr)
                rawX = motionEvent.rawX
                rawY = motionEvent.rawY
            }
            MotionEvent.ACTION_MOVE->{
                val xNow =  motionEvent.rawX
                val yNow =  motionEvent.rawY
                val absX = abs(xNow-rawX)
                val absY = abs(yNow-rawY)
                if (absX>15f||absY>15f){
                    params.x = arr[0]+ (xNow-rawX).toInt()
                    params.y = arr[1]+ (yNow-rawY).toInt()
                    windowManager.updateViewLayout(viewLayout,params)
                }
            }
            MotionEvent.ACTION_UP->{
                val xNow =  motionEvent.rawX
                val yNow =  motionEvent.rawY
                val absX = abs(xNow-rawX)
                val absY = abs(yNow-rawY)
                if (absX>15f||absY>15f){
                    //滑动
                    isSliding  = true
                }else{
                    //点击
                }
            }
        }
        return true
    }

    fun clearView(){
        windowManager.removeView(viewLayout)
        actionList.forEach {
            it.removeActionView()
        }
    }
    private var dialogMulti: AlertDialog? = null
    private fun showSettingMultiDialog() {
        if (dialogMulti==null){
            dialogMulti = AlertDialog.Builder(context,R.style.alertDialogStyle).apply {
                val view =  LayoutInflater.from(context).inflate(R.layout.layout_model_multi,null)
                setView(view)
                val binding = DataBindingUtil.bind<LayoutModelMultiBinding>(view)
                var setting = context.getSpValue("multi",WorkSetting())
                binding?.apply {
                    etInputDelay.setText(setting.click_interval.toString())
                    etInputSwipe.setText(setting.swipe_duration.toString())
                    listTypeDelay.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            setting.uint_click_interval = when(p2){
                                0->1
                                1->1000
                                else->60*1000
                            }
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }
                    }
                    listTypeSwipe.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            setting.uint_swipe_interval = when(p2){
                                0->1
                                1->1000
                                else->60*1000
                            }
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {

                        }
                    }
                    when (setting.uint_click_interval) {
                        1 -> listTypeDelay.setSelection(0)
                        1000 -> listTypeDelay.setSelection(1)
                        else -> listTypeDelay.setSelection(2)
                    }
                    when (setting.uint_swipe_interval) {
                        1 -> listTypeSwipe.setSelection(0)
                        1000 -> listTypeSwipe.setSelection(1)
                        else -> listTypeSwipe.setSelection(2)
                    }
                    when (setting.stop_model){
                        0->rbOne.isChecked  =true
                        1->rbSecond.isChecked  =true
                        2->rbThird.isChecked  =true
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
                        setting.click_interval = etInputDelay.text.toString().toLong()
                        setting.swipe_duration = etInputSwipe.text.toString().toLong()
                        setting.count_down = tvTimeAll.tag as Int
                        context.putSpValue("multi",setting)
                        dialogMulti?.dismiss()
                    }
                    tvTimeAll.setOnClickListener {
                        TimePicker(context,setting.count_down,object :TimePicker.TimeSetListener{
                            override fun onSaveTime(time: Int) {
                                tvTimeAll.text = time.getTimeFormat()
                                tvTimeAll.tag = time
                            }
                        },true)
                    }
                }
            }.create()
            dialogMulti?.window?.setType(2032)
        }
        dialogMulti?.show()
    }

    private var singleDialog: AlertDialog? = null
    private fun showSettingSingleDialog() {
        if (singleDialog==null){
            singleDialog = AlertDialog.Builder(context,R.style.alertDialogStyle).apply {
                val view = LayoutInflater.from(context).inflate(R.layout.layout_model_single,null)
                setView(view)
                val binding = DataBindingUtil.bind<LayoutModelSingleBinding>(view)
                var setting = context.getSpValue("single",WorkSetting())
                binding?.apply {
                    etInput.setText(setting.click_interval.toString())
                    when (setting.uint_click_interval) {
                        1 -> listType.setSelection(0)
                        1000 -> listType.setSelection(1)
                        else -> listType.setSelection(2)
                    }
                    when (setting.stop_model){
                        0->rbOne.isChecked  =true
                        1->rbSecond.isChecked  =true
                        2->rbThird.isChecked  =true
                    }
                    listType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            setting.uint_click_interval = when(p2){
                                0->1
                                1->1000
                                else->60*1000
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
                        context.putSpValue("single",setting)
                        singleDialog?.dismiss()
                    }
                    tvTimeAll.setOnClickListener {
                        TimePicker(context,setting.count_down,object :TimePicker.TimeSetListener{
                            override fun onSaveTime(time: Int) {
                                tvTimeAll.text = time.getTimeFormat()
                                tvTimeAll.tag = time
                            }
                        },true)
                    }
                }
            }.create()
            singleDialog?.window?.setType(2032)
        }
        singleDialog?.show()
    }
}