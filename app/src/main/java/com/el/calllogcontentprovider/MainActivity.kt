package com.el.calllogcontentprovider

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(stillNotGrantedPermission()) {
            ActivityCompat.requestPermissions(
                this,
                Array(1) {android.Manifest.permission.READ_CALL_LOG},
                101
            )
        }
        else {
            displayLog()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(
            requestCode == 101 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            displayLog()
        }
    }

    private fun displayLog() {
        
    }

    private fun stillNotGrantedPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CALL_LOG
        ) != PackageManager.PERMISSION_GRANTED
    }
}