package com.best.now.autoclick

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.best.now.autoclick.utils.getLoadingDialog
import com.gyf.immersionbar.ktx.immersionBar

abstract class BaseVMActivity : AppCompatActivity() {
    lateinit var mContext: Context
    protected inline fun <reified T : ViewDataBinding> binding(
            @LayoutRes resId: Int
    ): Lazy<T> = lazy {
        DataBindingUtil.setContentView<T>(this, resId).apply {
            lifecycleOwner = this@BaseVMActivity
        }
    }
    lateinit var loadingDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        loadingDialog = getLoadingDialog(this, null)
        initImmersionBar()
        initView()
        initData()
    }

    abstract fun initView()
    abstract fun initData()

    protected open fun initImmersionBar() {
       immersionBar {
//           fitsSystemWindows(true)
           transparentStatusBar()
           navigationBarColor(R.color.colorPrimary)
       }
    }
}