package com.himanshuhc.lincall

import android.app.Application
import com.himanshuhc.lincall.data.linphone.LinphoneManager

class LinCallApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LinphoneManager.init(this)
    }
}