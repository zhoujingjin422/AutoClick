package com.best.now.autoclick.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.best.now.autoclick.R
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class PublicHelper {

}



val adParentList = arrayListOf<LinearLayout>()

/*** 为控件加载广告 */
fun loadAd(linearLayout: LinearLayout) {

    if (linearLayout.childCount == 0) {
        var adView = AdView(linearLayout.context.applicationContext)
        adView.adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            linearLayout.context,
            SizeUtils.px2dp(ScreenUtils.getScreenWidth().toFloat())
        )
//        adView.adSize = AdSize.BANNER
            adView.adUnitId = Constant.AD_BANNER_ID
        adView.adListener = object :AdListener(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                LogUtils.e("adView:"+p0.message)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                LogUtils.e("adView:onAdLoaded")
            }
        }
        linearLayout.addView(adView)
    }

    var adView = linearLayout.getChildAt(0) as AdView

    linearLayout.visibility = View.VISIBLE
    adView.loadAd(AdRequest.Builder().build())

    if (!adParentList.contains(linearLayout)) {
        adParentList.add(linearLayout)
    }
}

/*** 更新广告控件 */
fun updateAdView() {
    for (adParentView in adParentList) {
        if (adParentView != null && adParentView.isAttachedToWindow) {
            // TODO: 广告一直显示
//            if (isPurchased()) {
            if (false) {
                var adView = adParentView.getChildAt(0) as AdView
                adView?.pause()
                adView?.destroy()

                adParentView.removeAllViews()
                adParentView.visibility = View.GONE
            } else {
                adParentView.visibility = View.VISIBLE
            }
        }
    }
}

var mInterstitialAd: InterstitialAd? = null

/*** 加载插页广告 */
fun loadInterstitialAd(context: Context) {
    InterstitialAd.load(
        context,
        Constant.AD_INTERSTITIAL_ID,
        AdRequest.Builder().build(),
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd

            }
        })
}

/*** 显示插屏广告 */
fun showInterstitialAd(activity: Activity, callback: (() -> Unit)? = null) {
    if (mInterstitialAd != null) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                mInterstitialAd = null
                loadInterstitialAd(activity)
                callback?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                mInterstitialAd = null
                callback?.invoke()
            }

            override fun onAdShowedFullScreenContent() {
            }
        }

        mInterstitialAd!!.show(activity)
    } else {
        loadInterstitialAd(activity)
        callback?.invoke()
    }

}

fun getLoadingDialog(
    context: Context,
    toastStr: String?,
    canceledOnTouchOutside: Boolean,
    cancelable: Boolean,
    backgroundDim: Float
): Dialog {
    val dialog = Dialog(context!!, R.style.CustomDialog)
    if (backgroundDim >= 0) {
        dialog.window!!.setDimAmount(backgroundDim)
    }
    val view: View = LayoutInflater.from(context).inflate(
        R.layout.dialog_loading, null
    )
    val toastTv = view.findViewById<TextView>(R.id.tv_toast)
    if (!TextUtils.isEmpty(toastStr)) {
        toastTv.text = toastStr
    } else {
        toastTv.text = "loading…"
    }

    setCustomerDialogAttributes(
        dialog,
        view,
        Gravity.CENTER,
        canceledOnTouchOutside,
        cancelable,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    return dialog
}

fun getLoadingDialog(context: Context, toastStr: String?): Dialog {
    return getLoadingDialog(context, toastStr, false, true, -1f)
}

fun setCustomerDialogAttributes(
    dlg: Dialog,
    contentView: View?, gravity: Int, canceledOnTouchOutside: Boolean,
    cancelable: Boolean, width: Int, height: Int
): TextView? {
    dlg.setCanceledOnTouchOutside(canceledOnTouchOutside)
    dlg.setCancelable(cancelable)
    dlg.setContentView(contentView!!)
    val window = dlg.window
    val params = window!!.attributes
    params.gravity = gravity
    params.width = width
    params.height = height
    //		window.setAttributes(params);
    //
    //		dlg.onWindowAttributesChanged(params);
    return null
}

/*** 指定位置到顶部 */
fun RecyclerView.positionToTop(position: Int, offset: Int? = 0) {
    //滑动滚动-会触发滚动事件
//    var mScroller = RecycleScrollTopScroller(this.context);
//    mScroller.targetPosition = position;
//    this.layoutManager?.startSmoothScroll(mScroller)

    //不会触发滚动事件，还能设置偏移量
    var layoutManager = this.layoutManager as LinearLayoutManager
    layoutManager.scrollToPositionWithOffset(position, offset!!)
//当有足够的项目填充屏幕时，它可以正常工作，但如果RecyclerView中只有少数项目，它会将它们向下推，并在屏幕顶​​部留下空白区域
//                    layoutManager.stackFromEnd = true//当item不满一屏时，从底部填充视图，顶部会空白

}

fun isServiceON(context: Context,className:String):Boolean{
    val manager:ActivityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
   val list = manager.getRunningServices(100)
    if (list.isNullOrEmpty())
        return false
    else{
        list.forEach {
            if (it.service.className==className)
                return true
        }
        return false
    }
}

