package com.best.now.autoclick.view

/**
author:zhoujingjin
date:2022/11/24
 */
 class WorkSetting:java.io.Serializable {
    /**
     * 点击间隔
     */
    var click_interval = 200L

    /**
     * 滑动时间
     */
    var swipe_duration = 200L

    /**
     * 滑动的单位
     */
    /**
     * 点击间隔单位，1是毫秒 1000毫秒  60000毫秒
     */
    var uint_swipe_duration = 1
    /**
     * 点击间隔单位，1是毫秒 1000毫秒  60000毫秒
     */
    var uint_click_interval = 1

    /**
     * 停止的模式 0是一直循环 1是几分钟后停止 3是循环几次停止
     */
    var stop_model = 0

    /**
     * 循环次数
     */
    var circle_count = 10

    /**
     * 倒计时时间，单位s
     */
    var count_down = 8*60

    companion object {
        private const  val serialVersionUID = -2211491L
    }
}