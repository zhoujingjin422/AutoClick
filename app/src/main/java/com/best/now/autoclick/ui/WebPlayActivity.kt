package com.best.now.autoclick.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.webkit.*
import androidx.databinding.DataBindingUtil
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.BuildConfig
import com.best.now.autoclick.R
import com.best.now.autoclick.bean.DataBean
import com.best.now.autoclick.databinding.ActivityWebPlayBinding
import com.best.now.autoclick.databinding.InputLayoutBinding
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ToastUtils
import java.util.*


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
    override fun initView() {
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
            }
            webView.addJavascriptInterface(JavaScriptObject(this@WebPlayActivity),"android")
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
            }
        }
    }

    private var url :String?= null
    override fun initData() {
         url = intent.getStringExtra("Url")
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
        fun goback() {
            activity.finish()
        }
    }

}