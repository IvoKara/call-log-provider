package com.el.calllogcontentprovider

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.database.getLongOrNull
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

    private var lastModified: Long = System.currentTimeMillis() / 1000

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

    override fun onStart() {
        super.onStart()

        getLastModifiedCallUnixDate()

        val tv = findViewById<TextView>(R.id.lastModifiedAll)
        tv.text = unixTimeToHumanReadable(lastModified)

        val btn = findViewById<Button>(R.id.syncButton)
        btn.setOnClickListener {
            storeLastModifiedCallUnixDate()
            Toast.makeText(applicationContext, "Time synced", Toast.LENGTH_LONG).show()
            tv.text = unixTimeToHumanReadable(lastModified)
            displayLog()
        }

        displayLog()
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

    private fun getLastModifiedCallUnixDate() {
        val sharedPref = this.getSharedPreferences("CallLog", Context.MODE_PRIVATE)
        val defaultValue = getLastCallDateInUnixTime()
        Log.d("Default value", unixTimeToHumanReadable(defaultValue))
        lastModified = sharedPref.getLong("lastModified", defaultValue)
        Log.d("From shared preferences", unixTimeToHumanReadable(lastModified))
    }

    private fun storeLastModifiedCallUnixDate() {
        val sharedPref = this.getSharedPreferences("CallLog", Context.MODE_PRIVATE)

        with (sharedPref.edit()) {
            putLong("lastModified", lastModified)
            apply()
        }
    }

    private fun displayLog() {
        val recentCallsCursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            colsFromContentProvider,
            "${CallLog.Calls.DATE} > $lastModified",
            null,
            "${CallLog.Calls.DATE} DESC"
        )

            with (recentCallsCursor) {
                if (this != null && count > 0) {
                    moveToFirst()
                    val colIndex = getColumnIndex(CallLog.Calls.DATE)
                    val lastCallDate = getLongOrNull(colIndex)
                    if (lastCallDate != null)
                        lastModified = lastCallDate
                }
        }

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

    private fun getLastCallDateInUnixTime() : Long {
        var lastCallDate: Long? = null
        val lastModifiedCursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            listOf<String>(CallLog.Calls.DATE).toTypedArray(),
            null, null,
            "${CallLog.Calls.DATE} DESC"
        )

        if (lastModifiedCursor != null) {
            lastModifiedCursor.moveToFirst()
            lastCallDate = lastModifiedCursor.getLongOrNull(0)
        }

        return lastCallDate ?: System.currentTimeMillis() / 1000
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