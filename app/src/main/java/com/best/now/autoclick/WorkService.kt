package com.best.now.autoclick

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.best.now.autoclick.ext.windowManager
import com.best.now.autoclick.view.MenuView

/**
author:zhoujingjin
date:2022/11/21
 */
class WorkService:AccessibilityService() {
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {

    }

    override fun onInterrupt() {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            "single"->{
//                windowManager
                MenuView(this,getSystemService(
                    WINDOW_SERVICE
                ) as WindowManager,true)
            }
            "multi"->{

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}