package com.best.now.autoclick.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.best.now.autoclick.R
import com.best.now.autoclick.ext.getSpValue
import com.best.now.autoclick.ext.sp

/**
author:zhoujingjin
date:2022/11/18
 */
class SplashActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //延迟两秒，判断是不是首次进入，首次进入到导航页，不是直接进首页
        findViewById<ConstraintLayout>(R.id.parent).postDelayed({
            if (getSpValue("First",true)){
                startActivity(Intent(this,GuideActivity::class.java))
            }else startActivity(Intent(this,MainActivity::class.java))
            finish()
        },1000L)
    }
}