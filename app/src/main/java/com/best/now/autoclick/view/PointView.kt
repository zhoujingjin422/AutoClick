package com.best.now.autoclick.view
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.best.now.autoclick.ext.dp
import java.util.jar.Attributes

class PointView@JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr:Int = 0, var bigWith:Int = 60.dp.toInt(), private val withText:Boolean, private val num:Int = 0): View(context,attributes,defStyleAttr) {
     var pointX = 0f
     var pointY = 0f
//    private var RADIUS = 42
    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor(/*if(withText) "#1a008cff" else */"#5CFA5900")
    }
    private val paint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = bigWith/15f
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

     fun setLocation() {
        val arr = IntArray(2)
        getLocationOnScreen(arr)
        val locationX = arr[0]
        pointX = (locationX+ bigWith/2).toFloat()
        pointY = (arr[1]+ bigWith/2).toFloat()
    }
     fun changeSize(size:Int){
        bigWith = size
        layoutParams.width = size
        layoutParams.height = size
        paint2.strokeWidth = bigWith/15f
        textPaint.textSize = bigWith*2/3f
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