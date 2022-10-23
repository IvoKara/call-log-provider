package com.el.calllogcontentprovider

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.*

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
            "${CallLog.Calls.DATE} DESC"
        )

        val fromColumns = listOf<String>(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
            CallLog.Calls.LAST_MODIFIED,
            CallLog.Calls.TYPE,
        ).toTypedArray()

        val applyToViewsId = intArrayOf(
            R.id.callerName,
            R.id.number,
            R.id.duration,
            R.id.date,
            R.id.lastModified,
            R.id.type,
        )

        val listview = findViewById<ListView>(R.id.listview)

        listview.adapter = object: SimpleCursorAdapter(
            applicationContext,
            R.layout.call_log_card,
            recentCallsCursor,
            fromColumns, applyToViewsId, 0
        ) {
            @SuppressLint("SimpleDateFormat")
            override fun setViewText(v: TextView?, text: String?) {
                var modifiedText = text
                when (v?.id) {
                    R.id.date, R.id.lastModified -> {
                        modifiedText = unixTimeToHumanReadable(text!!.toLong())
                    }
                    R.id.duration -> {
                        modifiedText = secondsToHumanReadable(text!!.toLong())
                    }
                    R.id.type -> {
                        modifiedText = callTypeToString(text!!.toInt())
                    }
                }
                super.setViewText(v, modifiedText)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun unixTimeToHumanReadable(unixTime: Long): String {
        val dateFormat = SimpleDateFormat(
            "HH:mm dd/MM/yyyy",
        )

        dateFormat.timeZone = TimeZone.getDefault()

        return dateFormat.format(unixTime).toString()
    }

    private fun secondsToHumanReadable(totalSecs: Long): String {
        val hours = totalSecs / 3600;
        val minutes = (totalSecs % 3600) / 60;
        val seconds = totalSecs % 60;

        return "${
            if (hours > 0) "${hours}h " else ""
        }${
            if (minutes > 0) "${minutes}min " else ""
        }${seconds}sec"
    }

    private fun callTypeToString(type: Int): String {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> "Incoming"
            CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
            CallLog.Calls.MISSED_TYPE -> "Missed"
            CallLog.Calls.REJECTED_TYPE -> "Rejected"
            else -> "Unknown"
        }
    }

    private fun stillNotGrantedPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CALL_LOG
        ) != PackageManager.PERMISSION_GRANTED
    }
}