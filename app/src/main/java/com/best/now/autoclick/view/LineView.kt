package com.best.now.autoclick.view


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

open class LineView(context: Context, private val lineWith:Int): View(context) {
    private var startX:Float = 0F
    private var startY:Float = 0F
    private var endX:Float = 0F
    private var endY:Float = 0F
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWith.toFloat()
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#C2FF5B00")
    }

     fun setEnd( endX:Int, endY:Int){
        this.endX = endX.toFloat()
        this.endY = endY.toFloat()
        invalidate()
    }
     fun setStart( startX:Int, startY:Int){
        this.startX = startX.toFloat()
        this.startY = startY.toFloat()
        invalidate()
    }
     fun setAllData(startX:Int, startY:Int,endX:Int, endY:Int){
        this.startX = startX.toFloat()
        this.startY = startY.toFloat()
        this.endX = endX.toFloat()
        this.endY = endY.toFloat()
        invalidate()
    }
    override fun onDraw(canvas: Canvas?) {
        canvas?.drawLine(startX,startY,endX,endY,paint)
    }
}