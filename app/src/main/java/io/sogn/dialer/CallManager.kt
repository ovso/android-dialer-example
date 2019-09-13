package io.sogn.dialer

import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Build
import android.telecom.Call
import android.telecom.TelecomManager
import android.util.Log

@TargetApi(Build.VERSION_CODES.M)
class CallManager private constructor(context: Context) {

    private val mTelecomManager: TelecomManager
    private var mCurrentCall: Call? = null
    private var mStateListener: StateListener? = null

    val uiCall: UiCall
        get() = UiCall.convert(mCurrentCall)

    interface StateListener {
        fun onCallStateChanged(call: UiCall)
    }

    init {
        Log.i(TAG, "init CallManager")

        mTelecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    }

    fun registerListener(listener: StateListener) {
        mStateListener = listener
    }

    fun unregisterListener() {
        mStateListener = null
    }

    fun updateCall(call: Call) {
        mCurrentCall = call

        if (mStateListener != null && mCurrentCall != null) {
            mStateListener!!.onCallStateChanged(UiCall.convert(mCurrentCall))
        }
    }

    fun placeCall(number: String) {
        val uri = Uri.fromParts("tel", number, null)
        mTelecomManager.placeCall(uri, null)
    }

    fun cancelCall() {
        if (mCurrentCall != null) {
            if (mCurrentCall!!.state == Call.STATE_RINGING) {
                rejectCall()
            } else {
                disconnectCall()
            }
        }
    }

    fun acceptCall() {
        Log.i(TAG, "acceptCall")

        if (mCurrentCall != null) {
            mCurrentCall!!.answer(mCurrentCall!!.details.videoState)
        }
    }

    private fun rejectCall() {
        Log.i(TAG, "rejectCall")

        if (mCurrentCall != null) {
            mCurrentCall!!.reject(false, "")
        }
    }

    private fun disconnectCall() {
        Log.i(TAG, "disconnectCall")

        if (mCurrentCall != null) {
            mCurrentCall!!.disconnect()
        }
    }

    companion object {
        private val TAG = "CallManager"

        private var sInstance: CallManager? = null

        fun init(applicationContext: Context): CallManager {
            if (sInstance == null) {
                sInstance = CallManager(applicationContext)
            } else {
                throw IllegalStateException("CallManager has been initialized.")
            }
            return sInstance as CallManager
        }

        fun get(): CallManager {
            checkNotNull(sInstance) { "Call CallManager.init(Context) before calling this function." }
            return sInstance as CallManager
        }
    }
}
