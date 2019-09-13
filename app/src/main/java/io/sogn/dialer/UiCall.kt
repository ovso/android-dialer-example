package io.sogn.dialer

import android.telecom.Call

/**
 * android.telecom.Call 객체를 UI에서 표현하기 위해 변환한 객체
 */
class UiCall private constructor() {

    var status: Status? = null
        private set
    private var mDisplayName: String? = null

    val displayName: String
        get() = if (mDisplayName != null) mDisplayName!! else UNKNOWN_NAME

    enum class Status private constructor(private val v: String) {
        CONNECTING("Connecting"),
        DIALING("Calling..."),
        RINGING("Incoming call"),
        ACTIVE(""),
        DISCONNECTED("Finished call"),
        UNKNOWN("");

        override fun toString(): String {
            return this.v
        }
    }

    init {
        status = Status.UNKNOWN
        mDisplayName = null
    }

    companion object {

        private val UNKNOWN_NAME = "Unknown"

        fun convert(call: Call?): UiCall {
            val uiCall = UiCall()

            if (call != null) {
                when (call.state) {
                    Call.STATE_ACTIVE -> uiCall.status = Status.ACTIVE
                    Call.STATE_RINGING -> uiCall.status = Status.RINGING
                    Call.STATE_CONNECTING -> uiCall.status = Status.RINGING
                    Call.STATE_DIALING -> uiCall.status = Status.DIALING
                    Call.STATE_DISCONNECTED -> uiCall.status = Status.DISCONNECTED
                    else -> uiCall.status = Status.UNKNOWN
                }

                uiCall.mDisplayName = call.details.handle.schemeSpecificPart
            }

            return uiCall
        }
    }
}
