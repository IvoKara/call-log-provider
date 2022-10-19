package com.el.calllogcontentprovider

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private val colsFromContentProvider = listOf<String>(
        CallLog.Calls._ID,
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.DURATION,
        CallLog.Calls.LAST_MODIFIED,
        CallLog.Calls.DATE,
        CallLog.Calls.CACHED_NAME,
        CallLog.Calls.CACHED_NUMBER_LABEL,
    ).toTypedArray()

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
        val recentCallsCursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            colsFromContentProvider,
            null, null,
            "${CallLog.Calls.LAST_MODIFIED} DESC"
        )

        val fromColumns = listOf<String>(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE,
        ).toTypedArray()

        val applyToViewsId = intArrayOf(
            R.id.callerName,
            R.id.number,
            R.id.duration,
            R.id.date,
            R.id.type,
        )

        val listview = findViewById<ListView>(R.id.listview)

        listview.adapter = SimpleCursorAdapter(
            applicationContext,
            R.layout.call_log_card,
            recentCallsCursor,
            fromColumns, applyToViewsId, 0
        )
    }

    private fun stillNotGrantedPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CALL_LOG
        ) != PackageManager.PERMISSION_GRANTED
    }
}