package io.sogn.dialer

import android.app.Application

class DialerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CallManager.init(this)
    }
}
