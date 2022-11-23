package com.best.now.autoclick.view

import android.accessibilityservice.GestureDescription

/**
author:zhoujingjin
date:2022/11/21
 */
abstract class BaseAutoClick {
    var description:GestureDescription? = null
    abstract fun removeActionView()
    abstract fun getGestureDescription(): GestureDescription
    abstract fun updateView(canMove:Boolean)
}