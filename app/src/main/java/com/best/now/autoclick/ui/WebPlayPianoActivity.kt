package com.best.now.autoclick.ui

import android.R.attr
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.BuildConfig
import com.best.now.autoclick.R
import com.best.now.autoclick.bean.DataBean
import com.best.now.autoclick.databinding.ActivityWebPlayPianoBinding
import com.best.now.autoclick.databinding.InputLayoutBinding
import com.best.now.autoclick.databinding.LayoutChooseBinding
import com.google.gson.Gson
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


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

    private var dialog:AlertDialog?= null
    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    override fun initView() {
        binding.apply {
//            toolBar.title = intent.getStringExtra("Title")
//            setSupportActionBar(toolBar)
//            toolBar.setNavigationOnClickListener {
//                onBackPressed()
//            }

            tvChange.setOnClickListener {
                dialog = AlertDialog.Builder(this@WebPlayPianoActivity).apply {
                    val input =  LayoutInflater.from(this@WebPlayPianoActivity).inflate(R.layout.input_layout,null)
                    setView(input)
                    val bind  = DataBindingUtil.bind<InputLayoutBinding>(input)
                    bind?.etUrl?.setText(url)
                    bind?.etUrl?.addTextChangedListener(object : TextWatcher {
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
            webView.addJavascriptInterface(JavaScriptObject(this@WebPlayPianoActivity),"android")
            webView.webViewClient = WebViewClient()
            webView.webChromeClient = object : com.tencent.smtt.sdk.WebChromeClient(){
//                override fun onShowFileChooser(
//                    webView: WebView?,
//                    filePathCallback: ValueCallback<Array<Uri>>?,
//                    fileChooserParams: FileChooserParams?
//                ): Boolean {
//                    uploadMessageAboveL = filePathCallback
//                    takePic()
//                    return true
//                }
                override fun onShowFileChooser(
                    p0: WebView?,
                    filePathCallback: com.tencent.smtt.sdk.ValueCallback<Array<Uri>>?,
                    p2: FileChooserParams?
                ): Boolean {
                    uploadMessageAboveL = filePathCallback
                    showPicDialog()
                    return true
                }
            }
        }
    }
    private var dialogChoose:AlertDialog? = null
    private fun showPicDialog(){
        dialogChoose = AlertDialog.Builder(this).apply {
            val view = LayoutInflater.from(this@WebPlayPianoActivity).inflate(R.layout.layout_choose,null)
            setView(view)
           val bind = DataBindingUtil.bind<LayoutChooseBinding>(view)
            bind?.apply {
                tvTakePhoto.setOnClickListener {
                    takePic()
                    dialogChoose?.dismiss()
                }
                tvChoosePhoto.setOnClickListener {
                    openFileChooseProcess()
                    dialogChoose?.dismiss()
                }
                tvCancel.setOnClickListener {
                    uploadMessageAboveL?.onReceiveValue(null)
                    uploadMessageAboveL = null
                    dialogChoose?.dismiss()
                }
            }
            setCancelable(false)
        }.create()
        dialogChoose?.show()
    }
    private var mImageUri: Uri? = null
    private var url :String?= null
    private fun openFileChooseProcess() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "*/*"
        startActivityForResult(Intent.createChooser(i, "test"), 101)
    }
    /**
     * 拍照
     */
   private fun takePic() {
        try {
            val state = Environment.getExternalStorageState()
            if (state == Environment.MEDIA_MOUNTED) {
                val cameraIntent = Intent()
               var mFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "temp.jpg"
                )
                ///storage/emulated/0/Pictures/temp.jpg
                mImageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    createImageUri()
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //content://com.example.shinelon.takephotodemo.provider/camera_photos/Pictures/temp.jpg
                    FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, mFile)
                } else {
                    Uri.fromFile(mFile)
                }
                //指定照片保存路径（SD卡），temp.jpg为一个临时文件，每次拍照后这个图片都会被替换
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                cameraIntent.action = MediaStore.ACTION_IMAGE_CAPTURE

//                cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(cameraIntent, 112)
            } else {
                val toast = Toast.makeText(this, "no SD card", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            uploadMessageAboveL?.onReceiveValue(null)
            uploadMessageAboveL = null
        }
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private fun createImageUri(): Uri? {
        val status = Environment.getExternalStorageState()
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        return if (status == Environment.MEDIA_MOUNTED) {
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        } else {
            contentResolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, ContentValues())
        }
    }
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
    var  uploadMessageAboveL:ValueCallback<Array<Uri>>? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (uploadMessageAboveL==null)
            return
        if (resultCode!= RESULT_OK){
            uploadMessageAboveL?.onReceiveValue(null)
            uploadMessageAboveL = null
            return
        }
        if (requestCode==112){
            mImageUri?.let {
                val arr = arrayOf(it)
                uploadMessageAboveL?.onReceiveValue(arr)
                uploadMessageAboveL = null
            }
            if (mImageUri==null){
                uploadMessageAboveL?.onReceiveValue(null)
                uploadMessageAboveL = null
            }
        }else if (requestCode==101){
            val uri = data?.data
            uploadMessageAboveL = if (uri==null){
                uploadMessageAboveL?.onReceiveValue(null)
                null
            }else{
                val arr = arrayOf(uri)
                uploadMessageAboveL?.onReceiveValue(arr)
                null
            }
        }
}
    class JavaScriptObject(private val activity: Activity) {
        @JavascriptInterface
        fun goback() {
            activity.finish()
        }
        @JavascriptInterface
        fun shareFn(str:String) {
            val dataBean = Gson().fromJson(str,DataBean::class.java)
            val bitmap = getNetImageByUrl(dataBean.url)
            val split = dataBean.url.split("/")
            val name = split[split.size-1]
            bitmap?.let {
                saveBitmap(name,activity,it)
            }
        }
        private fun getNetImageByUrl(strUrl: String): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                val url = URL(strUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.setConnectTimeout(6000) //超时设置
                connection.setDoInput(true)
                connection.setUseCaches(false) //设置不使用缓存
                connection.connect()
                val inputStream: InputStream = connection.getInputStream()
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return bitmap
        }
        private fun saveBitmap(imageName:String, activity: Context, bitmap: Bitmap) {
            //设置图片名称，要保存png，这里后缀就是png，要保存jpg，后缀就用jpg
            //Android Q  10为每个应用程序提供了一个独立的在外部存储设备的存储沙箱，没有其他应用可以直接访问您应用的沙盒文件
//            val f = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//            val file = File(f?.path + "/" + imageName) //创建文件
//            //        file.getParentFile().mkdirs();
//            try {
//                //文件输出流
//                val fileOutputStream = FileOutputStream(file)
//                //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
//                //写入，这里会卡顿，因为图片较大
//                fileOutputStream.flush()
//                //记得要关闭写入流
//                fileOutputStream.close()
//            } catch (e: FileNotFoundException) {
//                e.printStackTrace()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//            // 下面的步骤必须有，不然在相册里找不到图片，若不需要让用户知道你保存了图片，可以不写下面的代码。
//            // 把文件插入到系统图库
//            try {
//                MediaStore.Images.Media.insertImage(
//                    activity.contentResolver,
//                    file.absolutePath, imageName, null
//                )
//                Toast.makeText(activity, "save to album success", Toast.LENGTH_SHORT).show()
//            } catch (e: FileNotFoundException) {
//                Toast.makeText(activity, "save to album failed", Toast.LENGTH_SHORT).show()
//                e.printStackTrace()
//            }
//            //            // 最后通知图库更新
//            activity.sendBroadcast(
//                Intent(
//                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//                    Uri.fromFile(File(file.path))
//                )
//            )
            try {
                //获取要保存的图片的位图
                //创建一个保存的Uri
                val values = ContentValues()
                //设置图片名称
                values.put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    imageName
                )
                //设置图片格式
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                //设置图片路径
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                val saveUri = activity.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
                if (TextUtils.isEmpty(saveUri.toString())) {
                    Toast.makeText(activity, "save to album failed", Toast.LENGTH_SHORT).show()
                    return
                }
                saveUri?.let {
                    val outputStream= activity.contentResolver.openOutputStream(saveUri)
                    //将位图写出到指定的位置
                    //第一个参数：格式JPEG 是可以压缩的一个格式 PNG 是一个无损的格式
                    //第二个参数：保留原图像90%的品质，压缩10% 这里压缩的是存储大小
                    //第三个参数：具体的输出流
                    if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                        Toast.makeText(activity, "save to album success", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "save to album failed", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}