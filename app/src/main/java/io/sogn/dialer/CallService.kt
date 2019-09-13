package io.sogn.dialer

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log

@TargetApi(Build.VERSION_CODES.M)
class CallService : InCallService() {

    private val callCallback = object : Call.Callback() {

        override fun onStateChanged(call: Call, state: Int) {
            Log.i(TAG, "Call.Callback onStateChanged: " + call + "state: " + state)
            CallManager.get().updateCall(call)
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.i(TAG, "onCallAdded: $call")

        call.registerCallback(callCallback)
        val intent = Intent(this, CallActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        CallManager.get().updateCall(call)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.i(TAG, "onCallRemoved: $call")

        call.unregisterCallback(callCallback)
        CallManager.get().updateCall(null!!)
    }

    companion object {

        private val TAG = "CallService"
    }
}
