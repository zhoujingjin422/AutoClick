package com.best.now.autoclick

import android.app.Application
import com.best.now.autoclick.utils.AppOpenManager
import com.best.now.autoclick.utils.loadInterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheEntity
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.cookie.CookieJarImpl
import com.lzy.okgo.cookie.store.DBCookieStore
import com.lzy.okgo.model.HttpHeaders
import com.lzy.okgo.model.HttpParams
import com.tencent.smtt.sdk.QbSdk
import okhttp3.OkHttpClient
import java.util.*

/**
author:zhoujingjin
date:2022/11/27
 */
class AutoClickApplication:Application() {
    companion object {
        var appOpenManager: AppOpenManager? = null
    }
    override fun onCreate() {
        super.onCreate()
        initOkGo()
        MobileAds.initialize(this) {
            loadInterstitialAd(this)
            appOpenManager?.fetchAd()
        }

        appOpenManager = AppOpenManager(this)
        QbSdk.initX5Environment(this, object :QbSdk.PreInitCallback {


            override fun onCoreInitFinished() {
            }

            override fun onViewInitFinished(p0: Boolean) {
            }
        });
    }
    /*** 初始化OkGo */
    fun initOkGo() {

        //okGo网络框架初始化和全局配置

        //okGo网络框架初始化和全局配置
        val builder = OkHttpClient.Builder()

        //builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));      //使用sp保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));      //使用sp保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(CookieJarImpl(DBCookieStore(this))) //使用数据库保持cookie，如果cookie不过期，则一直有效

        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));      //使用内存保持cookie，app退出后，cookie消失
        //设置请求头
        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));      //使用内存保持cookie，app退出后，cookie消失
        //设置请求头
        val headers = HttpHeaders()
//        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文，不允许有特殊字符
//        headers.put("commonHeaderKey2", "commonHeaderValue2");
        //设置请求参数
        //        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文，不允许有特殊字符
//        headers.put("commonHeaderKey2", "commonHeaderValue2");
        //设置请求参数
        val params = HttpParams()
//        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
//        params.put("commonParamsKey2", "这里支持中文参数");
        //        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
//        params.put("commonParamsKey2", "这里支持中文参数");
        OkGo.getInstance().init(this) //必须调用初始化
//            .setOkHttpClient(builder.build()) //建议设置OkHttpClient，不设置会使用默认的
            .setCacheMode(CacheMode.FIRST_CACHE_THEN_REQUEST) //全局统一缓存模式，默认不使用缓存，可以不传
            .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE) //全局统一缓存时间，默认永不过期，可以不传
            .setRetryCount(0) //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
            .addCommonHeaders(headers) //全局公共头
            .addCommonParams(params)
    }
}