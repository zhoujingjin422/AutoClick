package com.best.now.autoclick

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.best.now.autoclick.ext.windowManager
import com.best.now.autoclick.ui.MainActivity
import com.best.now.autoclick.view.MenuView

/**
author:zhoujingjin
date:2022/11/21
 */
class WorkService:AccessibilityService() {
    private var menuView:MenuView? = null
    private fun changeModel(){
        menuView?.clearView()
        menuView = null
    }
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {

    }

    override fun onInterrupt() {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            MainActivity.SINGLEMODEL ->{
                changeModel()
                menuView =  MenuView(this,this,getSystemService(
                    WINDOW_SERVICE
                ) as WindowManager,true)
            }
            MainActivity.MULTIMODEL->{
                changeModel()
                menuView =  MenuView(this,this,getSystemService(
                    WINDOW_SERVICE
                ) as WindowManager,false)
            }
            MainActivity.DISABLEMODEL->{
                changeModel()
            }


        }
        return super.onStartCommand(intent, flags, startId)
    }
}