package com.best.now.autoclick

import android.content.Context
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.best.now.autoclick.databinding.LayoutTimePickerBinding


class TimePicker(context: Context,time:Int,onClickListener: TimeSetListener,withOutActivity:Boolean = false):AlertDialog(context,R.style.alertDialogStyle) {
    private var hour = 0
    private var minute = 0
    private var second = 0
   private var bind:LayoutTimePickerBinding? = null
    init {
       val view =  LayoutInflater.from(context).inflate(R.layout.layout_time_picker,null)
        setView(view)
         bind = DataBindingUtil.bind<LayoutTimePickerBinding>(view)
        bind?.apply {
            btnCancel.setOnClickListener { dismiss() }
            btnSave.setOnClickListener{
                onClickListener.onSaveTime(hour*3600+minute*60+second)
                dismiss()
            }
            npHour.minValue = 0
            npHour.maxValue = 24
            npMinute.minValue = 0
            npMinute.maxValue = 59
            npSecond.minValue = 0
            npSecond.maxValue = 59
            npHour.value = (time/3600)%25
            hour = (time/3600)%25
            npHour.setFormatter(format())
            npMinute.value = (time / 60) % 60
            minute = (time / 60) % 60
            npSecond.value = time % 60
            second = time % 60
            npMinute.setFormatter(format())
            npSecond.setFormatter(format())

            npHour.setOnValueChangedListener { _, _, i2 ->
                hour = i2
            }
            npMinute.setOnValueChangedListener { _, _, i2 ->
                minute = i2
            }
            npSecond.setOnValueChangedListener { _, _, i2 ->
                second = i2
            }
        }
        if (withOutActivity){
            window?.setType(2032)
        }
        setCanceledOnTouchOutside(true)
        show()
    }

    private fun format(): NumberPicker.Formatter {
        return NumberFormat()
    }

    class NumberFormat:NumberPicker.Formatter{
        override fun format(p0: Int): String {
            return String.format("%02d", Integer.valueOf(p0));
        }
    }
    interface TimeSetListener{
        fun onSaveTime(time: Int)
    }
}