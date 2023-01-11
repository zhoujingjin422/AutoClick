package com.best.now.autoclick.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.webkit.*
import android.webkit.WebChromeClient.CustomViewCallback
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.BuildConfig
import com.best.now.autoclick.R
import com.best.now.autoclick.databinding.ActivityWebPlayBinding
import com.best.now.autoclick.databinding.InputLayoutBinding
import com.best.now.autoclick.utils.loadAd
import com.best.now.autoclick.utils.showInterstitialAd


/*** 选择服务界面 */
class WebPlayActivity : BaseVMActivity() {
    private val binding by binding<ActivityWebPlayBinding>(R.layout.activity_web_play)
    companion object {
        fun startActivity(activity: Activity, title: String, url: String) {
            activity.startActivity(
                Intent(activity, WebPlayActivity::class.java)
                    .putExtra("Title", title).putExtra("Url", url)
            )
        }
    }

    private var dialog:AlertDialog?= null
    private var customViewCallback: CustomViewCallback? = null
    private var fullscreenContainer:FrameLayout?=null
    override fun initView() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;
        }
        binding.apply {
            tvChange.setOnClickListener {
                dialog = AlertDialog.Builder(this@WebPlayActivity).apply {
                    val input =  LayoutInflater.from(this@WebPlayActivity).inflate(R.layout.input_layout,null)
                    setView(input)
                    val bind  = DataBindingUtil.bind<InputLayoutBinding>(input)
                    bind?.etUrl?.setText(url)
                    bind?.etUrl?.addTextChangedListener(object :TextWatcher{
                        override fun beforeTextChanged(
                            p0: CharSequence?,
                            p1: Int,
                            p2: Int,
                            p3: Int
                        ) {

                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                        }
                    })
                    bind?.tvCancel?.setOnClickListener {
                        dialog?.dismiss()
                    }
                    bind?.tvOk?.setOnClickListener {
                        url = bind.etUrl.text.toString()
                        webView.loadUrl(url!!)
                        dialog?.dismiss()
                    }
                }.create()
                dialog?.show()
            }
            tvChange.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
            webView.settings.apply {
                useWideViewPort = true
                loadWithOverviewMode = true
                domStorageEnabled = true
                defaultTextEncodingName = "UTF-8"
                allowFileAccess = true
                allowContentAccess = true
                javaScriptEnabled = true
                pluginState = WebSettings.PluginState.ON
                allowFileAccess = true
//                cacheMode = WebSettings.LOAD_NO_CACHE
            }
            webView.addJavascriptInterface(JavaScriptObject(this@WebPlayActivity,webView),"android")
            webView.webViewClient = WebViewClient()
            webView.webChromeClient = object : WebChromeClient(){
                override fun onShowFileChooser(
                    p0: WebView?,
                    p1: ValueCallback<Array<Uri>>?,
                    p2: FileChooserParams?
                ): Boolean {
                    p2?.title
                    return super.onShowFileChooser(p0, p1, p2)
                }
                override fun onPermissionRequest(p0: PermissionRequest?) {
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                        p0?.grant(p0.resources)
                    }
                }

                override fun getVideoLoadingProgressView(): View? {
                    val frameLayout = FrameLayout(this@WebPlayActivity)
                    frameLayout.layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    return frameLayout
                }

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    showCustomView(view, callback)
                }

                override fun onHideCustomView() {
                    hideCustomView()
                }
            }
        }
    }
    private var customView:View? =null
    private fun showCustomView( view : View? , callback: WebChromeClient.CustomViewCallback?) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }

        window.decorView

        val  decor = window.decorView as FrameLayout
        val fullscreenContainer =  FullscreenHolder(this)
        fullscreenContainer.addView(view,FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ) )
        decor.addView(fullscreenContainer, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        customView = view
        setStatusBarVisibility(false)
        customViewCallback = callback
    }
    /** 隐藏视频全屏 */
    private fun hideCustomView() {
        if (customView == null) {
            return
        }

        setStatusBarVisibility(true)
        val decor = window.decorView as FrameLayout
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null
        customView = null
        customViewCallback?.onCustomViewHidden();
        binding.webView.visibility = View.VISIBLE;
    }
    private fun setStatusBarVisibility(visible:Boolean ) {
        val flag = if(visible)  0 else WindowManager.LayoutParams.FLAG_FULLSCREEN
        window.setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    private var url :String?= null
    override fun initData() {
        url = intent.getStringExtra("Url")
        url?.let {
            binding.webView.loadUrl(it)
        }
        loadAd(binding.advBanner)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.apply {
            stopLoading()
            clearView()
            destroy()
        }
    }


    class JavaScriptObject(private val activity: AppCompatActivity,private val webView: WebView) {
        @JavascriptInterface
        fun goback() {
            activity.runOnUiThread {
                activity.finish()
            }
        }
        @JavascriptInterface
        fun gamerestartadvert() {
            activity.runOnUiThread {
                showInterstitialAd(activity, callback = {
                    webView.evaluateJavascript("javascript:gamerestartadvert()"){

                    }
                })
            }
        }
        @JavascriptInterface
        fun gameadvert() {
            activity.runOnUiThread {
                showInterstitialAd(activity,callback = {
                    webView.evaluateJavascript("javascript:advertCallback()"){

                    }
                })
            }

        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode==KeyEvent.KEYCODE_BACK&&binding.webView.canGoBack()){
            binding.webView.goBack()
            return true
        }else if (keyCode==KeyEvent.KEYCODE_BACK){
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

class FullscreenHolder(context: Context):FrameLayout(context){
    init {
        setBackgroundColor(context.resources.getColor(android.R.color.black))
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }
}