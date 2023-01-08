package com.best.now.autoclick.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.best.now.autoclick.R
import com.best.now.autoclick.ext.getSpValue
import com.best.now.autoclick.ext.sp
import com.best.now.autoclick.utils.Constant

/**
author:zhoujingjin
date:2022/11/18
 */
class SplashActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }
        if (!isTaskRoot) {
            finish()
            return
        }
        //延迟两秒，判断是不是首次进入，首次进入到导航页，不是直接进首页
        findViewById<ConstraintLayout>(R.id.parent).postDelayed({
            if (getSpValue("First",true)){
                startActivity(Intent(this,GuideActivity::class.java))
            }else {
                WebPlayActivity.startActivity(this,"",Constant.URL_HOMEPAGE)
            }
            finish()
        },1000L)
    }
}