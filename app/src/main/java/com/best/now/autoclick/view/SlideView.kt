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
import com.best.now.autoclick.ext.dp
import kotlin.math.abs

class SlideView(context: Context, private val  windowManager: WindowManager, num:Int)/*:LineView(context,lineWith)*/:BaseAutoClick() {
    private val bigPointView = PointView(context, bigWith = 60.dp.toInt(), smallWith = 14.dp.toInt(), withText = true, num = num)
    private val smallPointView = PointView(context, bigWith = 60.dp.toInt() * 3 / 4, smallWith = 0, withText = false)
    private val line = LineView(context, 60.dp.toInt() * 3 / 5)
    var bigParams: WindowManager.LayoutParams? = null
    var smallParams: WindowManager.LayoutParams? = null
    var lineParams: WindowManager.LayoutParams? = null

    init {
        bigParams = setParams(bigPointView.bigWith)
        smallParams = setParams(smallPointView.bigWith)
        lineParams = setParams()
//        if (i3==-1){
        val withScreen = context.resources.displayMetrics.widthPixels
        val heightScreen = context.resources.displayMetrics.heightPixels
        bigParams?.x = (withScreen / 2 )- bigPointView.bigWith / 2
        bigParams?.y = (heightScreen / 3) - bigPointView.bigWith / 2
        smallParams?.x = (withScreen / 2 )- smallPointView.bigWith / 2
        smallParams?.y = (heightScreen * 2 / 3) - smallPointView.bigWith / 2
        bigPointView.setOnTouchListener(MyTouchListener(arr = IntArray(2),view = bigPointView,params = bigParams!!,slideView = this))
        smallPointView.setOnTouchListener(MyTouchListener(arr = IntArray(2),view = smallPointView,params = smallParams!!,slideView = this))
//        }else{
//            params1?.x = i3- m.bigWith/2
//            params1?.y = i4- m.bigWith/2
//            params2?.x = i5- n.bigWith/2
//            params2?.y = i6- n.bigWith/2
//        }
        updateView(bigPointView, bigParams!!.x, bigParams!!.y)
        updateView(smallPointView, smallParams!!.x, smallParams!!.y)
        windowManager.addView(line, lineParams)
        windowManager.addView(bigPointView, bigParams)
        windowManager.addView(smallPointView, smallParams)
    }

     fun updateView(view: View, x: Int, y: Int) {
        if (view == bigPointView) {
            val i4 = bigPointView.bigWith
            val i5 = y + i4 / 2
            line.setStart(x + i4 / 2, i5)
            return
        }
        val i6 = smallPointView.bigWith
        val i7 = y + i6 / 2
        line.setEnd(x + i6 / 2, i7)
    }
    fun updateViewLayout(view: View, x: Int, y: Int) {
        if (view == bigPointView) {
            bigParams?.x = x
            bigParams?.y = y
            windowManager.updateViewLayout(view,bigParams)
            return
        }
        smallParams?.x = x
        smallParams?.y = y
        windowManager.updateViewLayout(smallPointView,smallParams)
    }

    private fun setParams(with: Int): WindowManager.LayoutParams {
       /* val windowLayoutParams = WindowManager.LayoutParams()
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
        windowLayoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        windowLayoutParams.flags = windowLayoutParams.flags or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        windowLayoutParams.flags = windowLayoutParams.flags or (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        windowLayoutParams.format = PixelFormat.TRANSLUCENT
        windowLayoutParams.width = with
        windowLayoutParams.height = with*/
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
            windowLayoutParams.width =with
            windowLayoutParams.height = with
        windowLayoutParams.gravity = Gravity.TOP or Gravity.START
        return windowLayoutParams
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
        windowLayoutParams.flags = windowLayoutParams.flags or (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        windowLayoutParams.format = PixelFormat.TRANSPARENT
//        windowLayoutParams.gravity = Gravity.TOP or Gravity.START
        windowLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        windowLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        return windowLayoutParams
    }
    private var isSliding = false
    override fun removeActionView() {
        windowManager.removeView(bigPointView)
        windowManager.removeView(smallPointView)
        windowManager.removeView(line)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getGestureDescription(): GestureDescription {
        val path = Path()
        path.moveTo(smallPointView.pointX, smallPointView.pointY)
        path.lineTo(bigPointView.pointX, bigPointView.pointY)
       return GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, 500)).build()
    }
}
class MyTouchListener(private val arr:IntArray,private val view:PointView,private val params : WindowManager.LayoutParams,private val slideView: SlideView) : View.OnTouchListener{
    private var rawX = 0f
    private var rawY = 0f
    private var isSliding = false
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                v?.getLocationOnScreen(arr)
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
                    params.x = arr[0]+ (xNow-rawX).toInt()
                    params.y = arr[1]+ (yNow-rawY).toInt()
                    slideView.updateViewLayout(view,params.x,params.y)
                    slideView.updateView(view,params.x,params.y)
                }
            }
        }
        return true
    }
}