package com.best.now.autoclick.ui

import android.app.Activity
import android.content.Intent
import android.webkit.ValueCallback
import android.webkit.WebViewClient
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivityWebBinding
import com.best.now.autoclick.databinding.ActivityWebPlayBinding
import com.best.now.autoclick.databinding.ActivityWebPlayPianoBinding
import com.blankj.utilcode.util.LogUtils
import com.gyf.immersionbar.ktx.immersionBar


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

    override fun initImmersionBar() {

    }
    override fun initView() {
        binding.apply {
            webView.settings.apply {
                javaScriptEnabled = true
            }
            webView.webViewClient = WebViewClient()
        }
    }

    override fun initData() {
        val url = intent.getStringExtra("Url")
        url?.let {
            binding.webView.loadUrl(it)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        binding.webView.evaluateJavascript("javascript:window.android.backFn({})") {
            LogUtils.e("javascript$it")
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
}