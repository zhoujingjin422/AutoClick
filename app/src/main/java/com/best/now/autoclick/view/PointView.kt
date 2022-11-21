package com.best.now.autoclick.view
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.best.now.autoclick.ext.dp
import java.util.jar.Attributes

class PointView@JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr:Int = 0, var bigWith:Int, private val smallWith:Int, private val withText:Boolean, private val num:Int = 0): View(context,attributes,defStyleAttr) {

//    private var RADIUS = 42
    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor(/*if(withText) "#1a008cff" else */"#5CFA5900")
    }
    private val paint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4.dp
        color = Color.parseColor("#D84827")
    }
    private val paint3 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#E9390D")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        textSize = bigWith*2/3f
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setLocation()
    }

    private fun setLocation() {
        val arr = IntArray(2)
        getLocationOnScreen(arr)
        val locationX = arr[0]
        val x = locationX+ bigWith/2
        val y = arr[1]+ bigWith/2
    }
    public fun changeSize(size:Int){
        bigWith = size
        layoutParams.width = size
        layoutParams.height = size
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
       val w =  width/2f
        val h = height/2f
        canvas?.drawCircle(w,h, (bigWith/2-bigWith/15).toFloat(),paint)
        canvas?.drawCircle(w,h, (bigWith/2-bigWith/15).toFloat(),paint2)
        if (withText){
            canvas?.drawCircle(w,h,bigWith/10f,paint3)
        }
        if (num>0)
            canvas?.drawText(num.toString(),w-textPaint.measureText(num.toString())/2f,h-(textPaint.descent()+textPaint.ascent())/2f,textPaint)
    }
}