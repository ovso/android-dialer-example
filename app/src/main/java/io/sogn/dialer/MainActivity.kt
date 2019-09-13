package io.sogn.dialer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonCall.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }
        checkDefaultDialer()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            checkSetDefaultDialerResult(resultCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (REQUEST_CODE_GRANT_PERMISSIONS == requestCode) {
            var check = 0

            for (result in grantResults) {
                check += result
            }

            if (check < 0) {
                Toast.makeText(this, "need more permissions", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.buttonCall) {
            val number = textNumber.text.toString()

            if (number.length > 0) {
                CallManager.get().placeCall(number)
            }
        }
    }

    /**
     * 기본 Dialer를 체크하고 변경
     */
    private fun checkDefaultDialer() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        var isAlreadyDefaultDialer: Boolean

        try {
            isAlreadyDefaultDialer = telecomManager.defaultDialerPackage == packageName
        } catch (e: NullPointerException) {
            isAlreadyDefaultDialer = false
        }

        if (isAlreadyDefaultDialer) {
            return
        }

        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
        startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
    }

    private fun checkSetDefaultDialerResult(resultCode: Int) {
        val message: String

        when (resultCode) {
            Activity.RESULT_OK -> message = "기본 전화 앱으로 설정하였습니다."
            Activity.RESULT_CANCELED -> message = "기본 전화 앱으로 설정하지 않았습니다."
            else -> message = "Unexpected result code $resultCode"
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissions() {
        if (ifNeededRequestPermission()) {
            ActivityCompat.requestPermissions(
                this, sRequiredPermissions,
                REQUEST_CODE_GRANT_PERMISSIONS
            )
        }
    }

    private fun ifNeededRequestPermission(): Boolean {
        var check = 0

        /**
         * 하나라도 permission이 없으면 check는 음수가 됨
         */
        for (permission in sRequiredPermissions) {
            check += ActivityCompat.checkSelfPermission(this, permission)
        }

        return check < 0
    }

    companion object {

        private val REQUEST_CODE_SET_DEFAULT_DIALER = 100
        private val REQUEST_CODE_GRANT_PERMISSIONS = 200

        private val sRequiredPermissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.MANAGE_OWN_CALLS
        )
    }
}
