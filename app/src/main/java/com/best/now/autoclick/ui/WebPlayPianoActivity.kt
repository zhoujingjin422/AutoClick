package com.best.now.autoclick.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivityWebPlayPianoBinding


/*** 选择服务界面 */
class WebPlayPianoActivity : BaseVMActivity() {
    private val binding by binding<ActivityWebPlayPianoBinding>(R.layout.activity_web_play_piano)
    companion object {
        fun startActivity(activity: Activity, title: String, url: String) {
            activity.startActivity(
                Intent(activity, WebPlayPianoActivity::class.java)
                    .putExtra("Title", title).putExtra("Url", url)
            )
        }
    }


    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    override fun initView() {
        binding.apply {
            toolBar.title = intent.getStringExtra("Title")
            setSupportActionBar(toolBar)
            toolBar.setNavigationOnClickListener {
                onBackPressed()
            }
            val webSettings = webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.allowFileAccess = true
            webSettings.allowFileAccessFromFileURLs = false
            webSettings.allowUniversalAccessFromFileURLs = false
            webSettings.builtInZoomControls = false

            webSettings.useWideViewPort = true
            webSettings.loadWithOverviewMode = true
            webView.isHorizontalScrollBarEnabled = false
            webView.isVerticalScrollBarEnabled = false
            webSettings.domStorageEnabled = true
            webSettings.blockNetworkImage = false
            webSettings.javaScriptCanOpenWindowsAutomatically = true
            webSettings.loadsImagesAutomatically = true
            webView.addJavascriptInterface(JavaScriptObject(this@WebPlayPianoActivity),"android")
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

    class JavaScriptObject(private val activity: Activity) {
        @JavascriptInterface
        fun backFn(str:String) {
            activity.finish()
        }
    }
}