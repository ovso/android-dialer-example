package io.sogn.dialer

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import kotlinx.android.synthetic.main.activity_call.*
import java.util.*

/**
 * Incomming / Outgoing Call 화면
 */
class CallActivity : Activity(), View.OnClickListener, CallManager.StateListener, Handler.Callback {

    private var mTimer: Timer? = null
    private var mElapsedTime: Long = 0
    private var mHandler: Handler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        hideBottomNavigationBar()

        buttonHangup.setOnClickListener(this)
        buttonAnswer.setOnClickListener(this)

        updateView(CallManager.get().uiCall)

        mHandler = Handler(this)
    }

    override fun onResume() {
        super.onResume()
        CallManager.get().registerListener(this)
    }

    override fun onPause() {
        super.onPause()
        CallManager.get().unregisterListener()
    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.buttonHangup -> CallManager.get().cancelCall()
            R.id.buttonAnswer -> CallManager.get().acceptCall()
        }

    }

    private fun hideBottomNavigationBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    override fun onCallStateChanged(uiCall: UiCall) {
        updateView(uiCall)
    }

    /**
     * 현재 전화 상태에 따라 view 모양 변경
     *
     * @param uiCall
     */
    private fun updateView(uiCall: UiCall) {
        textStatus.visibility =
            if (uiCall.status === UiCall.Status.ACTIVE) View.GONE else View.VISIBLE

        textStatus.text = uiCall.status!!.toString()

        textDuration.visibility =
            if (uiCall.status === UiCall.Status.ACTIVE) View.VISIBLE else View.GONE

        buttonHangup.visibility =
            if (uiCall.status === UiCall.Status.DISCONNECTED) View.GONE else View.VISIBLE

        if (uiCall.status === UiCall.Status.DISCONNECTED) {
            Handler().postDelayed({ finish() }, 3000)
        }

        if (uiCall.status === UiCall.Status.ACTIVE) {
            startTimer()
        } else if (uiCall.status === UiCall.Status.DISCONNECTED) {
            stopTimer()
        }

        textDisplayName.text = uiCall.displayName

        buttonAnswer.visibility =
            if (uiCall.status === UiCall.Status.RINGING) View.VISIBLE else View.GONE
    }

    private fun startTimer() {
        stopTimer()

        mElapsedTime = 0L
        mTimer = Timer()
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                mElapsedTime++
                mHandler!!.sendEmptyMessage(MSG_UPDATE_ELAPSEDTIME)
            }
        }, 0, PERIOD_MILLIS)
    }

    private fun stopTimer() {
        if (mTimer != null) {
            mTimer!!.cancel()
            mTimer = null
        }
    }

    private fun toDurationString(time: Long): String {
        return String.format(
            Locale.US,
            "%02d:%02d:%02d",
            time / 3600,
            time % 3600 / 60,
            time % 60
        )
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_UPDATE_ELAPSEDTIME -> textDisplayName.text = toDurationString(mElapsedTime)
        }
        return true
    }

    companion object {

        private val TAG = "CallActivity"

        private val PERIOD_MILLIS = 1000L
        private val MSG_UPDATE_ELAPSEDTIME = 100
    }
}
