package com.best.now.autoclick.view

import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import kotlin.math.abs

class DrugPointView(context: Context, private val windowManager: WindowManager, bigWith:Int, smallWith:Int, num:Int) : View.OnTouchListener,BaseAutoClick() {
    private val pointView = PointView(context,bigWith = bigWith,smallWith = smallWith,withText = true,num = num)
    private var params:WindowManager.LayoutParams?=null
    private val arr = IntArray(2)
    private var rawX = 0f
    private var rawY = 0f
    private var isSliding = false
    init {
        pointView.setOnTouchListener(this)
        params = setParams(bigWith)
        val withScreen = context.resources.displayMetrics.widthPixels
        val heightScreen = context.resources.displayMetrics.heightPixels
        params?.x = (withScreen / 2 )- pointView.bigWith / 2
        params?.y = (heightScreen / 3) - pointView.bigWith / 2
        windowManager.addView(pointView, params)
    }
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                pointView.getLocationOnScreen(arr)
                rawX = event.rawX
                rawY = event.rawY
            }
            MotionEvent.ACTION_UP->{
               val xNow =  event.rawX
               val yNow =  event.rawY
                val absX = abs(xNow-rawX)
                val absY = abs(yNow-rawY)
                if (absX>15f||absY>15f){
                    //滑动
                    isSliding  = true
                }else{
                    //点击
                }
            }
            MotionEvent.ACTION_MOVE->{
                val xNow =  event.rawX
                val yNow =  event.rawY
                val absX = abs(xNow-rawX)
                val absY = abs(yNow-rawY)
                if (absX>15f||absY>15f){
                    params?.x = arr[0]+ (xNow-rawX).toInt()
                    params?.y = arr[1]+ (yNow-rawY).toInt()
                    pointView.setLocation()
                    windowManager.updateViewLayout(pointView,params)
                }
            }
        }
        return true
    }
    private fun setParams(with: Int): WindowManager.LayoutParams {
        val   windowLayoutParams =  WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
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
        windowLayoutParams.width =with
        windowLayoutParams.height = with
        windowLayoutParams.gravity = Gravity.TOP or Gravity.START
        return windowLayoutParams
    }

    override fun removeActionView() {
        windowManager.removeView(pointView)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getGestureDescription(): GestureDescription {
        val path = Path()
        path.moveTo(pointView.pointX, pointView.pointY)
        return GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, 5)).build()
    }
}