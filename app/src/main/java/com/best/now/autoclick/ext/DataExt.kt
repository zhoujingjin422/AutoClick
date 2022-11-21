package com.best.now.autoclick.ext

import android.content.Context
import java.text.SimpleDateFormat

/**
 * author：miaofeng
 * created on: 2021/4/30 10:13.
 * description: 日期操作工具类
 */



/**
 * 日期格式 yyyy-MM-dd HH:mm:ss
 */
var dateTimeFormatter1 = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss")

/**
 * 日期格式 yyyy-MM-dd
 */
val Context.dateTimeFormatter2 get() = SimpleDateFormat(
        "yyyy-MM-dd")



var dateTimeFormatter2 = SimpleDateFormat(
        "yyyy-MM-dd")

/**
 * 日期格式 yyyy-MM-dd HH:mm
 */
var dateTimeFormatter3 = SimpleDateFormat(
        "yyyy-MM-dd HH:mm")

/**
 * 日期格式 MM-dd
 */
var dateTimeFormatter4 = SimpleDateFormat(
        "MM-dd")

/**
 * 时间格式 HH:mm
 */
var timeFormatter1 = SimpleDateFormat(
        "HH:mm")

/**
 * 时间格式 HH:mm:ss
 */
var timeFormatter2 = SimpleDateFormat(
        "HH:mm:ss")