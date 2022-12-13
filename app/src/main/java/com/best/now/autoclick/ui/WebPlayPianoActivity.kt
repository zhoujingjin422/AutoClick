package com.best.now.autoclick.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebViewClient
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.NOData
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivityWebBinding
import com.best.now.autoclick.databinding.ActivityWebPlayBinding
import com.best.now.autoclick.databinding.ActivityWebPlayPianoBinding
import com.blankj.utilcode.util.LogUtils
import com.gyf.immersionbar.ktx.immersionBar


/*** 选择服务界面 */
class WebPlayPianoActivity : BaseVMActivity(),JSInterface {
    private val binding by binding<ActivityWebPlayPianoBinding>(R.layout.activity_web_play_piano)
    companion object {
        fun startActivity(activity: Activity, title: String, url: String) {
            activity.startActivity(
                Intent(activity, WebPlayPianoActivity::class.java)
                    .putExtra("Title", title).putExtra("Url", url)
            )
        }
    }

    override fun initImmersionBar() {

    }
    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    override fun initView() {
        binding.apply {
            webView.settings.javaScriptEnabled = true
            webView.settings.javaScriptCanOpenWindowsAutomatically = true
            webView.addJavascriptInterface(this,"android")
            webView.webViewClient = WebViewClient()
        }
    }

    override fun initData() {
        val url = intent.getStringExtra("Url")
        url?.let {
            binding.webView.loadUrl(it)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding.webView.apply {
            stopLoading()
            clearView()
            destroy()
        }
    }

    override fun backFn() {
        onBackPressed()
    }

    class JSInterface{
        @JavascriptInterface
        public fun backFn(){

        }
    }
}