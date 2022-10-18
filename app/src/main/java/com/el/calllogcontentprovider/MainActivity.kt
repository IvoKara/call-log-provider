package com.el.calllogcontentprovider

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.widget.ListView
import android.widget.SimpleCursorAdapter
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
        val colsFromContentProvider = listOf<String>(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.LAST_MODIFIED,
            CallLog.Calls.DATE,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.CACHED_NUMBER_LABEL,
        ).toTypedArray()

        val from = listOf<String>(
            CallLog.Calls.NUMBER,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        ).toTypedArray()

        val to = intArrayOf(
            R.id.textView1,
            R.id.textView2,
            R.id.textView3
        )

        val recentCalls = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            colsFromContentProvider,
            null, null,
            "${CallLog.Calls.LAST_MODIFIED} DESC"
        )

        val adapter = SimpleCursorAdapter(
            applicationContext,
            R.layout.activity_call_log,
            recentCalls,
            from, to, 0
        )

        val listview = findViewById<ListView>(R.id.listview)

        listview.adapter = adapter
    }

    private fun stillNotGrantedPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CALL_LOG
        ) != PackageManager.PERMISSION_GRANTED
    }
}