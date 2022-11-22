package com.best.now.autoclick.view

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.LayoutDrugViewBinding
import com.best.now.autoclick.ext.dp
import kotlin.math.abs

/**
author:zhoujingjin
date:2022/11/21
 */
class MenuView(context:Context,accessibilityService: AccessibilityService,private val windowManager: WindowManager,singleMode:Boolean = true):View.OnTouchListener {
    private  var binding: LayoutDrugViewBinding? = null
    private var params:WindowManager.LayoutParams
    private val arr = IntArray(2)
    private var rawX = 0f
    private var rawY = 0f
    private var isSliding = false
    private var actionList = mutableListOf<BaseAutoClick>()
    private val viewLayout =  LayoutInflater.from(context).inflate(R.layout.layout_drug_view,null)
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
                val slideView = SlideView(context,windowManager,actionList.size+1)
                actionList.add(slideView)
            }
        }
        if (singleMode){
            val clickView =  DrugPointView(context,windowManager,60.dp.toInt(),14.dp.toInt(),0)
            actionList.add(clickView)
        }
        params = setParams()
        params.x = 0
        params.y = 100
        windowManager.addView(viewLayout,params)
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
}