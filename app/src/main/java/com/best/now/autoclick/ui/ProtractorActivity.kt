package com.best.now.autoclick.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.SurfaceHolder
import android.view.View
import com.best.now.autoclick.BaseVMActivity
import com.best.now.autoclick.R
import com.best.now.autoclick.camera.CameraManager
import com.best.now.autoclick.databinding.ActivityProtractorBinding
import java.io.IOException

class ProtractorActivity:BaseVMActivity(), SurfaceHolder.Callback {
    private val binding by binding<ActivityProtractorBinding>(R.layout.activity_protractor)
    private var hasSurface = false
    private var cameraOn = true
    override fun initView() {
        binding.apply {
            flBack.setOnClickListener { finish() }
            cameraSwicth.setOnClickListener {
                cameraOn = !cameraOn
                if (cameraOn) {
                    cameraSwicth.setBackgroundResource(R.drawable.shape_button_camera)
                    if (surface != null) {
                        surface.visibility = View.VISIBLE
                    }
                    startPreview()
                } else {
                    cameraSwicth.setBackgroundResource(R.drawable.shape_button_camera_off)
                    if (surface != null) {
                        surface.visibility = View.INVISIBLE
                    }
                    stopPreview()
                }
            }
        }
        val animator = ObjectAnimator.ofFloat(binding.flBack,"rotation",0f,270f)
        animator.duration = 50
        animator.start()
        val animatorTitle = ObjectAnimator.ofFloat(binding.title,"rotation",0f,270f)
        animatorTitle.duration = 50
        animatorTitle.start()
    }

    override fun onResume() {
        super.onResume()
        startPreview()
    }
    private fun startPreview(){
        val holder = binding.surface.holder
        if (hasSurface){
            initCamera(holder)
        }else{
            holder.addCallback(this)
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }
    }
    private fun initCamera(surfaceHolder: SurfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder)
        } catch (ioe: IOException) {
            return
        } catch (e: RuntimeException) {
            return
        }
    }
    override fun initData() {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (!hasSurface) {
            hasSurface = true
            initCamera(holder)
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        hasSurface = false
    }
    private fun stopPreview() {
        CameraManager.get().stopPreview()
        CameraManager.get().closeDriver()
    }

    override fun onDestroy() {
        CameraManager.get().stopPreview()
        super.onDestroy()
    }
    override fun onPause() {
        super.onPause()
        stopPreview()
    }
}